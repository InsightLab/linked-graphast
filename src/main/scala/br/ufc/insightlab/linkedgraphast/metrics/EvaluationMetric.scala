package br.ufc.insightlab.linkedgraphast.metrics

trait EvaluationMetric {
  def apply(originalDataPath: String, generatedDataPath: String, skipRow: Int = 1, delimiter: String = "\t"): Double
}
