package scala.download

import java.io.{Closeable, File, BufferedOutputStream, FileOutputStream, OutputStream, InputStream}
import java.net.{HttpURLConnection, URL}
import swing._
import sys.process._

object Status extends Enumeration {
	type Status = Value
	val active, inactive, stopped = Value
}

class DownloadThread(columnCount: Int, urlString: String, fileName: String) {
  private val thread = new Thread(Swing.Runnable(connector))
  var metrics = Array(urlString) ++ new Array[String](columnCount - 2) ++ Array(fileName)
  private var status = Status.inactive
  
  def start {
    if(status.equals(Status.inactive) && metrics(1) != "100%") {
      thread.start
      status = Status.active
    }
  }
  
  def pause {
    thread.suspend
    status = Status.inactive
  }
  
  def resume {
    thread.resume
    status = Status.active
  }
  
  def delete {
    thread.stop
    status = Status.stopped
  }
  
	def printThreadData = {
		println("thread priority: " + thread.getPriority)
		println("thread id: " + thread.getId)
		println("thread name: " + thread.getName)
	}

	private def withResource[T <: Closeable, R](res: T)(func: T => R) =
		try { func(res) }
		finally {
			try { res.close() }
			catch { case e: Exception => e.printStackTrace() }
		}
	
	def connector {
//  	val connection = new URL(urlString).openConnection
//  	val out = new BufferedOutputStream(new FileOutputStream(fileName))
//
//    withResource(ProgressInputStream(connection.getInputStream, connection.getContentLength, tracker)) {
//		  inputStream => val buffer = new Array[Byte](1024)
//		  Iterator.continually(inputStream.read(buffer)).takeWhile(_ != -1).foreach(n => out.write(buffer, 0, n))
	  }
	}

	def tracker(p: Progress) {
		formMetrics(p.formattedMetrics.toArray)
		if(metrics(1) == "100%") delete
	}
	
	def isActive : Boolean = {
	  thread.isAlive()
	}
	
	def formMetrics(array: Array[String]) {
	  metrics = Array(urlString) ++ array ++ Array(fileName)
	}
	
	def getUrlString: String = urlString
	
	def getFileName: String = fileName
}