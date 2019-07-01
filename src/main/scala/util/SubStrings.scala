package scala.util

class SubStrings(sample : String, start : String, end : String) {
  
  private val endIndexes = indexesOf(end)
  private val startIndexes = indexesOf(start)
  
  private def sortedJoinedIndexes(i : Int) = joinIndexes(i).sorted
  
  private def joinIndexes(i : Int) = startIndexes ++  Array(endIndexes(i))
  
  private def indexesOf(pattern : String)  = 0.until(sample.length).filter(sample.startsWith(pattern, _))
  
  private def startIndexSub(i : Int) = joinIndexes(i)(sortedJoinedIndexes(i).indexOf(endIndexes(i)) - 1)
  
  private def allSub(i : Int) = sample.substring(startIndexSub(i) , endIndexes(i) + end.length)
  
  def getAllSub : Vector[String] = {
    if(endIndexes.isEmpty) return Vector("ends.isEmpty")
    if(startIndexes.isEmpty) return Vector("starts.isEmpty")
    def loop(i : Int, list : Vector[String]) : Vector[String] = {
      val newList = list ++ Vector(allSub(i))
      if(i < endIndexes.length - 1) return loop(i + 1, newList)
      newList
    }
    loop(0, Vector[String]())
  }
}