package br.ufc.insightlab.linkedgraphast.modules.fragmentextractor

import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.SimilarityKeywordMatcherFilteringWords
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity.{JaroWinkler, SimilarityMetric}
import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.query.steinertree.Neo4jSteinerTree

object Neo4jFragmentExtractor {

  def apply(graph: LinkedGraph, metric: SimilarityMetric)(search: String): LinkedGraph = {
    Neo4jSteinerTree(graph)((new SimilarityKeywordMatcherFilteringWords(JaroWinkler, metric))(graph)(search).toList)
  }

}
