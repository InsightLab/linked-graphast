package metrics

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.io.Source

object Recall extends EvaluationMetric {
  override def apply(originalDataPath: String, generatedDataPath: String, skipRow: Int, delimiter: String): Double = {
    val generatedDataLines = Source.fromFile(generatedDataPath).getLines()
    generatedDataLines.next()

    var generatedData: ArrayBuffer[Array[String]] =
      generatedDataLines
      .map(_.split(delimiter)).to[ArrayBuffer]

    var totalData, foundData = 0.0

    val originalDataLines = Source.fromFile(originalDataPath).getLines()
    originalDataLines.next()

    for{
      line <- originalDataLines
      if line.length > 1
      values = line.split(delimiter)
    }{
      totalData += values.size

      var found = false
      var i = 0

      while(!found && i<generatedData.size){
        if(values.forall(generatedData(i).contains)){
          foundData += values.size
          generatedData.remove(i)
          found = true
        }
        i+=1
      }
    }
    println(s"Found: $foundData | Total: $totalData")
    foundData/totalData
  }
}
