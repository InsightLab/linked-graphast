package br.ufc.insightlab.linkedgraphast.modules.vonqbe

import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.SimilarityKeywordMatcherOptimizedWithFilters
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity.{JaroWinkler, PermutedSimilarity}
import br.ufc.insightlab.linkedgraphast.modules.querybuilder.{MultipleSchemaSPARQLQueryBuilder, NERQueryBuilder, SchemaSPARQLQueryBuilder}
import br.ufc.insightlab.linkedgraphast.query.steinertree.SteinerTree

class VonQBESparqlBuilder(graph: LinkedGraph, initNER: Boolean = false) {

  private var ner: Option[NERQueryBuilder] =
    if(initNER)
      Some(new NERQueryBuilder(new PermutedSimilarity(JaroWinkler)))
    else None

  def generateSPARQL(text: String, withNER: Boolean = false): String = {

    if(withNER)
      if(ner.isDefined) ner.get(graph)(text)
      else {
        ner = Some(new NERQueryBuilder(new PermutedSimilarity(JaroWinkler)))
        ner.get(graph)(text)
      }
    else{
      val (nodes,filters) = new SimilarityKeywordMatcherOptimizedWithFilters(new PermutedSimilarity(JaroWinkler))(graph)(text)

      val fragment = SteinerTree(graph)(nodes.toList)

      SchemaSPARQLQueryBuilder(fragment,filters, graph)
//      MultipleSchemaSPARQLQueryBuilder(List((fragment, filters)), graph)
    }

  }

}
