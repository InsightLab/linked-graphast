package br.ufc.insightlab.linkedgraphast.metrics

import org.slf4j.LoggerFactory

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.io.Source

object Recall extends EvaluationMetric {

  private val logger = LoggerFactory.getLogger(this.getClass)

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
    logger.debug(s"Found: $foundData | Total: $totalData")
    if(foundData == 0 || totalData == 0) 0
    else foundData/totalData
  }
}
