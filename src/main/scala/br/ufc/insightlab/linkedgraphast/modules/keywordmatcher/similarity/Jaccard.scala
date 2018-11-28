package br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity

import info.debatty.java.stringsimilarity.Jaccard

object Jaccard extends SimilarityMetric {
  val jc = new Jaccard()

  override def apply(a: String, b: String): Double =
    jc.similarity(a,b)
}
