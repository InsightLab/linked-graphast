package experiments

import org.insightlab.linkedgraphast.modules.keywordmatcher.SimilarityKeywordMatcherOptimizedWithFilters
import org.insightlab.linkedgraphast.modules.keywordmatcher.similarity.{JaroWinkler, PermutedSimilarity, SimilarityMetric}
import org.insightlab.linkedgraphast.model.graph.LinkedGraph
import org.insightlab.linkedgraphast.query.steinertree.SteinerTree

abstract class Experiment(t: String, graph: LinkedGraph) {
  private val filterRegex = "\\[([a-zA-Z]*|<[=]?|>[=]?|[!]?=);[^\\]]*\\]"
  protected val text = t.replaceAll(filterRegex,"")

  def preProcess: String

  def run(expectedFragment: LinkedGraph, metrics: List[SimilarityMetric], times: Int = 100): List[List[Double]] = {

    (for (_ <- 1 to times) yield {
      val experimentText = preProcess
//      println(experimentText)
      for(metric <- metrics) yield{
        val (nodes, _) = new SimilarityKeywordMatcherOptimizedWithFilters(metric,0.8)(graph)(experimentText)
        //      println(nodes)
        val fragment = SteinerTree(graph)(nodes.toList)

        val links = fragment.getLinksAsStream.toList

        expectedFragment.getLinksAsStream.count(links.contains).toDouble/expectedFragment.getNumberOfEdges
      }

    }).toList
  }

}
