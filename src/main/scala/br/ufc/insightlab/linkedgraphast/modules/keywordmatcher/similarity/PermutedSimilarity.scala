package br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity

import scala.collection.mutable.ListBuffer

class PermutedSimilarity(metric: SimilarityMetric) extends SimilarityMetric {

  def apply(a: String, b: String): Double = {
    val tokens = a.split(" ")
    var targets = ListBuffer[String]() ++= b.split(" ")

    val similarities = for (t <- tokens) yield {
      var i = -1

      val similars = targets.map(x => {
        i += 1
        (i,metric(t, x))
      })

      val max = if(similars.nonEmpty){
        val m:(Int,Double) = similars.maxBy(_._2)
        targets.remove(m._1)
        m
      } else (-1,0.0)

      max._2

    }

    similarities.sum / math.max(a.split(" ").length,b.split(" ").length)
  }

}

