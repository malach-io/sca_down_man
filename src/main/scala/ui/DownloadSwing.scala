package scala.ui

import java.net.URL
import java.awt.Dimension
import java.io.Closeable
import javax.swing.KeyStroke

import scala.collection.mutable.ArrayBuffer
import swing._
import swing.event._
import scala.concurrent.future

import scala.download.DownloadThread
import scala.util.UtilIO

object DownloadSwing extends SimpleSwingApplication {

  val modelColumnNames = List("address", "percent", "size", "done", "remain", "bps", "elapsed", "estimated", "save to")
  val tableModel = new MyTableModel( Array[Array[Any]](), modelColumnNames)

  val activeThreadsLimit = 4

	val urlTextField = new TextField(35)
	val table = new Table { model = tableModel }
	var threadList = ArrayBuffer[DownloadThread]()
	val buttonStart = Button("Start") { start(getRowNumber) }
	val buttonPause = Button("Pause") { pause(getRowNumber) }
	val buttonResume = Button("Resume") { resume(getRowNumber) }
  val buttonDelete = Button("Delete") { delete(getRowNumber) }
	
	def top = new MainFrame {
		title = "File Downloader"
		
		menuBar = new MenuBar {
      contents += new Menu("Options") {
      contents += new MenuItem(new Action("Add URL") {
        accelerator = Some(KeyStroke.getKeyStroke("ctrl U"))
        def apply { addURLFrame }
      })
   
      contents += new MenuItem(new Action("Add List") {
        accelerator = Some(KeyStroke.getKeyStroke("ctrl L"))
        def apply { addBatchFileFrame }
      })
   
      contents += new MenuItem(new Action("Add with Grabber") {
        accelerator = Some(KeyStroke.getKeyStroke("ctrl G"))
        def apply { addBatchURLFrame }
      })
   
      contents += new MenuItem(new Action("Save") {
        accelerator = Some(KeyStroke.getKeyStroke("ctrl S"))
        def apply { save }
      })
	  
	  contents += new MenuItem(new Action("Exit") {
        accelerator = Some(KeyStroke.getKeyStroke("ctrl Q"))
        def apply { quit }
      })
      }
    }

		contents = new BoxPanel(Orientation.Vertical) {
//			contents += new FlowPanel(FlowPanel.Alignment.Left)(
//					new Label("URL:"), urlTextField, Button("Download") { download })					
			contents += new ScrollPane(table)
			contents += new FlowPanel(FlowPanel.Alignment.Left)(buttonStart, buttonPause, buttonResume, buttonDelete)
		}

		size = new Dimension(700, 250)
		centerOnScreen
	}
	    
  def saveChooser : String = {  
    val chooser = new FileChooser
    val result = chooser.showOpenDialog(null)
    if (result == FileChooser.Result.Approve) return chooser.selectedFile.toString
    ""
  }
  
  def addURLFrame {
    val newFrame = new Frame{ secondFrame =>
      title   = "Add URL"
      visible = true
      val urlTextField = new TextField(35)
      contents = new FlowPanel {
        contents += new Label("URL:")
        contents += urlTextField
        contents += new Button(Action("Download") {
          secondFrame.dispose
          download(urlTextField.text)
        })
		  }
    }
  }
  
  def addBatchURLFrame {
    val newFrame = new Frame{ secondFrame =>
      title   = "Add with Grabber"
      visible = true
      val urlTextField = new TextField(35)
      contents = new FlowPanel {
        val typeBox = new ComboBox(List("",".jpg",".png",".mp3",".bmp"))
        listenTo(typeBox.selection)
		contents += new Label("Type:")
        contents += typeBox
        contents += new Label("URL:")
        contents += urlTextField
        contents += new Button(Action("Download") {
          val fileType = typeBox.selection.item
          if(!fileType.isEmpty && !urlTextField.text.isEmpty) {
            batchURLtoList(urlTextField.text, fileType)
            secondFrame.dispose 
          }
        })
		  }
    }
  }
  
  def addBatchFileFrame {
    val fileName = saveChooser.toString
    if(!fileName.isEmpty)batchFileToList(fileName)
  }
  
  def save {
    val list = threadList.map { x => x.getUrlString }.toList.distinct
    val write = new UtilIO
    val fileName = saveChooser.toString
    if(!fileName.isEmpty && !threadList.isEmpty) write.writeListToFile(fileName, list)
  }

	def download(urlString : String) {
		println(s"urlString $urlString")
	  if(startsWithHttp(urlString)){
	    val fileName = saveChooser.toString
			println(fileName)
	    if(!fileName.isEmpty) urlToList(urlString, fileName)
	  }
	}
	
	def urlToList(urlString : String, fileName : String) {
	  tableModel.addBlankRow
  	urlTextField.peer.setText("")
	  val thread = new DownloadThread(tableModel.getColumnCount, urlString, fileName)
  	threadList += thread
	  if(belowActiveThreadsLimit) thread.start
	  table.revalidate
	  update
	}

	def URLfileName(urlString : String) : String = {
	  val name = urlString.split("/")
	  name(name.length - 1)
	}

	def batchToList(batch : List[String]) {
	  batch.foreach { x => urlToList(x, "c://temp-dl/" + URLfileName(x)) }
	}

	def batchURLtoList(address : String, fileType : String) {
	  val batch = new UtilIO
	  batchToList(batch.downloadGetSubURL(address, fileType))
	}

	def batchFileToList(fileName : String) {
	  val file = new UtilIO
	  batchToList(file.readFile(fileName).toList)
	}

	def start(row : Int) = if(belowActiveThreadsLimit && !threadList.isEmpty) threadList(row).start

	def pause(row : Int) = threadList(row).pause

	def resume(row : Int) = threadList(row).resume

	def delete(row : Int) {
	  if(tableHasRow){
  	  println(row)
  	  threadList(row).delete
	    threadList.remove(row)
	    tableModel.removeRow(row)
	  }
	}

//  def setButtonsEnabled(pauseBool: Boolean, resumeBool: Boolean, cancelBool: Boolean, removeBool: Boolean) {
//    buttonPause.setEnabled(pauseBool);
//    buttonResume.setEnabled(resumeBool);
//    buttonCancel.setEnabled(cancelBool);
//    buttonRemove.setEnabled(removeBool);
//  }
//
//  def updateButtons() {
//    if (selectedDownloader != null) {
//        int state = selectedDownloader.getState();
//        switch (state) {
//            case Downloader.DOWNLOADING: setButtonsEnabled(true, false, true, false);
//                break;
//            case Downloader.PAUSED: setButtonsEnabled(false, true, true, false);
//                break;
//            case Downloader.ERROR: setButtonsEnabled(false, true, false, true);
//                break;
//            default: setButtonsEnabled(false, false, false, true);
//        }
//    } else setButtonsEnabled(false, false, false, false);
//  }

	def update {
	  def updater(x : Int) {
	    if(tableHasRow)
	      tableModel.setRowValue(threadList(x).metrics, x)
	      threadList.toList.foreach { x => if(belowActiveThreadsLimit) x.start }
	      if(x < threadList.length - 1) updater(x + 1)
	      table.repaint
	  }

	  eventTimer(updater)
	}

	def eventTimer(f : (Int) => Unit) {
	  val timer = new javax.swing.Timer(250, Swing.ActionListener(e => f(0)))
    timer.start
    if (!timer.isRunning()) eventTimer(f)
	}

	def getActiveThreads : Int = {
	  threadList.count { x => x.isActive }
	}

	def belowActiveThreadsLimit = getActiveThreads <= activeThreadsLimit
	
	def getRowNumber : Int = {
	  var x = 0
	  for (c <- table.selection.rows) x = c
	  x
  }
	
	def startsWithHttp(urlString : String) : Boolean = {
		def startsWith(header: String) = urlString.toLowerCase().startsWith(header)
		startsWith("http://") || startsWith("https://")
	}
	
	def tableHasRow : Boolean = !threadList.isEmpty
}