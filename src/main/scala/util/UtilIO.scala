package scala.util

import scala.io.Source
import sys.process._
import java.net.URL
import java.io.{File, PrintWriter}
import java.nio.file.{Path, Paths, Files}

class UtilIO {
  
  private def writeToFile(f: File)(op: PrintWriter => Unit) {
    val p = new PrintWriter(f)
    try { op(p) } finally { p.close() }
  }
  
  def writeListToFile(address : String, data : List[Any]) = writeToFile(new File(address)) { p => data.foreach(p.println) }
  
  def deleteFile(path : String) {
    new File(path).delete
  }
  
  def indexesOf(master : String)(pattern : String) {
    0.until(master.length).filter(master.startsWith(pattern, _))
  }
    
  def reverse(s: String) = ("" /: s)((a, x) => x + a)
  
  def downloadFile(url: String)(fileName: String) = {
    new URL(url) #> new File(fileName) !!
	}
  
  def downloadToCache(url : String) {
    val path = cacheTempFile.replace("\\", "/")
    downloadFile(url)(path)
  }
  
  def cacheTempFile : String = {
    val f = File.createTempFile("d://tmp", ".txt")
    val path = f.getAbsolutePath
    f.deleteOnExit()
    path
  }

  def readFileByLine(fileName : String)(f : (String) => Unit) {
    for(line <- readFile(fileName)) f(line)
  }
  
  def readFile(fileName : String) = Source.fromFile(fileName).getLines()
  
  def readFindSub(fileName : String)(start : String, end : String) = {
    readFile(fileName)
		  .filter { x => x.contains(start) && x.contains(end) }
		  .map { x => new SubStrings(x, start, end).getAllSub }
		  .toList.distinct.flatten
  }
  
  def downloadGetSubURL(address : String, fileType : String) : List[String] = {
    val tempPath = cacheTempFile.replace("\\", "/")
	  downloadFile(address)(tempPath)
    val fileStrings1 = readFindSub(tempPath)("http", fileType)
    val fileStrings2 = readFindSub(tempPath)(""""/""", fileType).map { x => (address + x).replace(""""/""", "") }
    deleteFile(tempPath)
    fileStrings1 ++ fileStrings2
  }
}