package br.ufc.insightlab.linkedgraphast.modules.keywordmatcher

import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.model.node.LinkedNode
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity.SimilarityMetric
import org.insightlab.graphast.model.Node

class SimilarityKeywordMatcher(metric: SimilarityMetric, threshold: Double = 0.9) extends KeywordMatcher {

  require(threshold <= 1.0)
  require(threshold >= 0.0)

  override def apply(graph: LinkedGraph)(text: String): Set[Node] = {
    val tokens = text.toLowerCase.split(" ")

    var nodes = Set[Node]()

    var composedTerm = ""
    var lastNodeFound: Option[Long] = None
    for (token <- tokens) {
      //      println(s"Token: $token")
      val testTerm = if (composedTerm == "") token else composedTerm + s" $token"
      //      println(s"Testing term: $testTerm")
      val (nodeId, isCompletable) = getMostSimilarTerm(testTerm, graph.getLiterals)

      if (nodeId != -1 || isCompletable) {
        //        println(s"Saving as Composed Term and nodeId = $nodeId")
        composedTerm = testTerm
        lastNodeFound = if (nodeId != -1) Some(nodeId) else None
      }
      else {
        //        println(s"No results. Discarding composed term")
        if (lastNodeFound.isDefined) {
          nodes += graph.getNode(lastNodeFound.get)
          lastNodeFound = None
        }
        else {
          val (id, _) = getMostSimilarTerm(composedTerm, graph.getLiterals)
          if (id != -1) nodes += graph.getNode(id)
        }

        composedTerm = token
      }
    }
    if (composedTerm != "") {
      val (nodeId, _) = getMostSimilarTerm(composedTerm, graph.getLiterals)
      if (nodeId != -1)
        nodes += graph.getNode(nodeId)
    }

    nodes
  }

  private def getMostSimilarTerm(term: String, terms: Stream[LinkedNode]): (Long, Boolean) = {
    var isCompletable = false

    def checkSimilarityAndCompletability(t: String): Double =
      if (t.toLowerCase.startsWith(term)) {
        //        println(s"Term $term can be completed to ${t.split("\\^\\^<").head}")
        isCompletable = true
        metric(term, t.split("\\^\\^<").head.toLowerCase)
      }
      else metric(term, t.split("\\^\\^<").head.toLowerCase)

    val mostSimilars = terms
      //      .map(t => (t,checkSimilarityAndCompletability(t.value)))
      .flatMap(t => {
      val s = checkSimilarityAndCompletability(t.value)
      if(s >= threshold) List((t, s))
      else List()
    })
      //      .filter(_._2 >= threshold)
      .sortBy(-_._2)

    //    println(mostSimilars)

    if (mostSimilars.nonEmpty) (mostSimilars.head._1.getId, isCompletable)
    else (-1, isCompletable)


  }

}

