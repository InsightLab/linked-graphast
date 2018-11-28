package br.ufc.insightlab.linkedgraphast.modules.vonqbe

import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.SimilarityKeywordMatcherOptimizedWithFilters
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity.{JaroWinkler, PermutedSimilarity}
import br.ufc.insightlab.linkedgraphast.modules.querybuilder.SchemaSPARQLQueryBuilder
import br.ufc.insightlab.linkedgraphast.query.steinertree.SteinerTree

class VonQBESparqlBuilder(graph: LinkedGraph) {

  def generateSPARQL(text: String): String = {
    val (nodes,filters) = new SimilarityKeywordMatcherOptimizedWithFilters(new PermutedSimilarity(JaroWinkler))(graph)(text)

    val fragment = SteinerTree(graph)(nodes.toList)

    SchemaSPARQLQueryBuilder(fragment,filters, graph)
  }

}
