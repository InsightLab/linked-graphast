package br.ufc.insightlab.linkedgraphast.modules.fragmentextractor

import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.SimilarityKeywordMatcherOptimized
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity.SimilarityMetric
import br.ufc.insightlab.linkedgraphast.query.steinertree.SteinerTree

object FragmentExtractor {
  def apply(graph: LinkedGraph, metric: SimilarityMetric)(search: String): LinkedGraph = {
    SteinerTree(graph)((new SimilarityKeywordMatcherOptimized(metric))(graph)(search).toList)
  }
}
