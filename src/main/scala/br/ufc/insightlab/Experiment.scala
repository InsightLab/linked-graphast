package br.ufc.insightlab

import br.ufc.insightlab.linkedgraphast.modules.fragmentextractor.FragmentExtractor
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.{SimilarityKeywordMatcherOptimized, SimilarityKeywordMatcherOptimizedWithFilters}
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity.{JaroWinkler, PermutedSimilarity}
import br.ufc.insightlab.linkedgraphast.modules.querybuilder.SchemaSPARQLQueryBuilder
import br.ufc.insightlab.linkedgraphast.parser.NTripleParser
import br.ufc.insightlab.linkedgraphast.query.steinertree.SteinerTree

object Experiment extends App {

  val graph = NTripleParser.parse("src/main/resources/dbpedia.nt")

  val (nodes,filters) = new SimilarityKeywordMatcherOptimizedWithFilters(new PermutedSimilarity(JaroWinkler))(graph)("person[contains;John] Books")
  println(nodes.mkString(","))
  println(filters.mkString("\n"))

  val fragment = SteinerTree(graph)(nodes.toList)
  println(fragment.linksAsString())

  val query = SchemaSPARQLQueryBuilder(fragment,filters,graph)

  println(query)

}
