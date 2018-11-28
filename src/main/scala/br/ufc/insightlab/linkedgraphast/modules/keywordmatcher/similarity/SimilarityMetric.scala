package br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity

trait SimilarityMetric {
  def apply(a: String, b: String): Double
}
