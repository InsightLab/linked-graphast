package br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity

import info.debatty.java.stringsimilarity.JaroWinkler

object JaroWinkler extends SimilarityMetric {
  val jw = new JaroWinkler()

  override def apply(a: String, b: String): Double =
    jw.similarity(a,b)

}
