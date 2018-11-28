package br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity


object NGram extends SimilarityMetric {
  val ng = new info.debatty.java.stringsimilarity.NGram()

  override def apply(a: String, b: String): Double = {
    val d = math.abs(ng.distance(a,b))

    if(d > 0) 1/d
    else 0
  }

}
