package br.ufc.insightlab.linkedgraphast.modules.vonqbe

import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.modules.NER.NERClassifier
import br.ufc.insightlab.linkedgraphast.modules.NER.wikifier.Wikifier
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.SimilarityKeywordMatcherOptimizedWithFilters
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity.{JaroWinkler, PermutedSimilarity}
import br.ufc.insightlab.linkedgraphast.modules.querybuilder.{MultipleSchemaSPARQLQueryBuilder, NERQueryBuilder, SchemaSPARQLQueryBuilder}
import br.ufc.insightlab.linkedgraphast.query.steinertree.SteinerTree

class VonQBESparqlBuilder(graph: LinkedGraph, threshold: Double = 0.9, nerClassifier: Option[NERClassifier] = None) {

  def this(graph: LinkedGraph, nerClassifier: NERClassifier) =
    this(graph, 0.9, Some(nerClassifier))

  def this(graph: LinkedGraph, threshold: Double, nerClassifier: NERClassifier) =
    this(graph, threshold, Some(nerClassifier))

  def this(graph: LinkedGraph, threshold: Double) =
    this(graph, threshold, Some(Wikifier))

  private val ner: Option[NERQueryBuilder] =
    if(nerClassifier.isDefined)
      Some(new NERQueryBuilder(nerClassifier.get, new PermutedSimilarity(JaroWinkler), threshold))
    else None

  def generateSPARQL(text: String, withNER: Boolean = false, withMinimalPaths: Boolean = false): String = {

    if(withNER && ner.isDefined) ner.get(graph, withMinimalPaths)(text)
    else{
      val (nodes,filters) = new SimilarityKeywordMatcherOptimizedWithFilters(new PermutedSimilarity(JaroWinkler), threshold)(graph)(text)
//      println(nodes)
      val fragment = SteinerTree(graph, withMinimalPaths)(nodes.toList)

      SchemaSPARQLQueryBuilder(fragment,filters, graph)
//      MultipleSchemaSPARQLQueryBuilder(List((fragment, filters)), graph)
    }

  }

}
