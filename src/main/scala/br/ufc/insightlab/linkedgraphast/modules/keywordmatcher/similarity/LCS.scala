package br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity

import info.debatty.java.stringsimilarity.LongestCommonSubsequence

object LCS extends SimilarityMetric {
  val lcs = new LongestCommonSubsequence()

  override def apply(a: String, b: String): Double =
    lcs.distance(a,b) / math.max(a.length, b.length)

}
