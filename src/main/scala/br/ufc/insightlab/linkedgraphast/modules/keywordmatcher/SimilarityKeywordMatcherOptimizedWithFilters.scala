package br.ufc.insightlab.linkedgraphast.modules.keywordmatcher

import org.insightlab.graphast.model.Node
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity.SimilarityMetric
import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class SimilarityKeywordMatcherOptimizedWithFilters(metric: SimilarityMetric, threshold: Double = 0.9) {

  private val filterPattern = "(\\[([a-zA-Z]*|<[=]?|>[=]?|[!]?=);[^\\]]*\\])".r

  require(threshold <= 1.0)
  require(threshold >= 0.0)

  def apply(graph: LinkedGraph)(text: String): (ListBuffer[Node],Map[Long,List[String]]) = {
    val tokens = text.toLowerCase.split(" ").toList

    var nodes = ListBuffer[Node]()

    val subsequences: IndexedSeq[IndexedSeq[String]] =
      for(i <- tokens.indices)
        yield
          for(j <- i+1 to tokens.size) yield {tokens.slice(i,j) mkString " "}

    val filterOccurence = subsequences.map(_.map(filterPattern.findAllMatchIn(_).toList.map(_.toString)))

    var mostSimilars = subsequences.map(l => mutable.Seq.fill[(Node, Double)](l.size)((new Node(-1),0)))

    for{
      literal <- graph.getLiterals
      cleanLiteral = literal.value.split("\\^\\^<").head.toLowerCase.split("@").head
      i <- subsequences.indices
      j <- subsequences(i).indices
    }{
      val s: Double =
        if(filterOccurence(i)(j).nonEmpty)
          metric(cleanLiteral,filterPattern.replaceAllIn(subsequences(i)(j),""))
        else metric(cleanLiteral,subsequences(i)(j))
      if(s >= threshold && mostSimilars(i)(j)._2 < s) {
        mostSimilars(i)(j) = (literal, s)
      }
    }

    var filters = Map[Long,List[String]]()
    var i = 0
    while(i < mostSimilars.size){
      //take the candidates from the last to the first, getting the larger match
      val index = mostSimilars(i).size - mostSimilars(i).reverse.takeWhile(_._1.getId == -1).size
      if(index != 0){
        val node = mostSimilars(i)(index-1)._1
        nodes += node
        val filter = filterOccurence(i)(index-1)

        if(filter.nonEmpty)
          filters += node.getId -> filter

        val jump = index
        i = i + jump
      }
      else i += 1

    }

    (nodes, filters)
  }

}
