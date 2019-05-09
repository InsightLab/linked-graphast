package br.ufc.insightlab.linkedgraphast.modules.keywordmatcher

import br.ufc.insightlab.graphast.model.Node
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity.SimilarityMetric
import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph

import scala.collection.mutable

class SimilarityKeywordMatcherOptimized
(metric: SimilarityMetric, threshold: Double = 0.9) extends KeywordMatcher {

  require(threshold <= 1.0)
  require(threshold >= 0.0)

  override def apply(graph: LinkedGraph)(text: String): Set[Node] = {
    val tokens = text.toLowerCase.split(" ").toList

    var nodes = Set[Node]()

    val subsequences: IndexedSeq[IndexedSeq[String]] =
      for(i <- tokens.indices)
        yield
          for(j <- i+1 to tokens.size) yield {tokens.slice(i,j) mkString " "}

    var mostSimilars = subsequences.map(l => mutable.Seq.fill[(Node, Double)](l.size)((new Node(-1),0)))

    for{
      literal <- graph.getLiterals
      cleanLiteral = literal.value.split("\\^\\^<").head.toLowerCase.split("@").head
      i <- subsequences.indices
      j <- subsequences(i).indices
    }{
      val s: Double = metric(cleanLiteral,subsequences(i)(j))
      if(s >= threshold && mostSimilars(i)(j)._2 < s) {
        mostSimilars(i)(j) = (literal, s)
      }
    }



    var i = 0
    while(i < mostSimilars.size){
      //take the candidates from the last to the first, getting the larger match
      val index = mostSimilars(i).size - mostSimilars(i).reverse.takeWhile(_._1.getId == -1).size
      if(index != 0){
        nodes += mostSimilars(i)(index-1)._1
        val jump = index
        i = i + jump
      }
      else i += 1

    }

    nodes
  }

}
