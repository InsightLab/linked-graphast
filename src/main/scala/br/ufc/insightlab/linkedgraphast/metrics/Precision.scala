package br.ufc.insightlab.linkedgraphast.metrics

import scala.io.Source

object Precision extends EvaluationMetric {
  override def apply(originalDataPath: String, generatedDataPath: String, skipRow: Int, delimiter: String): Double = {
    val recall = Recall(originalDataPath, generatedDataPath)

    val totalGot: Double = math.max(Source.fromFile(generatedDataPath).getLines().count(!_.equals("\n")) - 1, 0)
    val totalExpected: Double = math.max(Source.fromFile(originalDataPath).getLines().count(!_.equals("\n")) - 1, 0)

//    println(s"Total got: $totalGot | Total expected: $totalExpected")
    if(totalExpected == 0 || totalGot == 0) 0
    else totalExpected*recall/totalGot
  }
}
