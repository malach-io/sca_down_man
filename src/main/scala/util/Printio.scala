package scala.util

import java.io.{File, PrintWriter}

class Printio {
  def printToFile(f: File)(op: PrintWriter => Unit) {
    val p = new PrintWriter(f)
    try { op(p) } finally { p.close() }
  }
  
  def printToFile2(address : String, data : List[Any]) = printToFile(new File(address)) { p => data.foreach(p.println) }
}