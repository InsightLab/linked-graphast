package br.ufc.insightlab.linkedgraphast.modules.vonqbe

import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.SimilarityKeywordMatcherOptimizedWithFilters
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity.{JaroWinkler, PermutedSimilarity}
import br.ufc.insightlab.linkedgraphast.query.steinertree.SteinerTree

class VonQBEFragmentExtractor(graph: LinkedGraph) {

  def generateFragment(search: String): LinkedGraph = {
    val (nodes,filters) = new SimilarityKeywordMatcherOptimizedWithFilters(new PermutedSimilarity(JaroWinkler))(graph)(search)
    SteinerTree(graph)(nodes.toList)
  }

}
