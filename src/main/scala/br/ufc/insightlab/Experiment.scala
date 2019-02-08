package br.ufc.insightlab

import br.ufc.insightlab.linkedgraphast.modules.figer.Figer
import br.ufc.insightlab.linkedgraphast.modules.fragmentexpansor.FragmentExpansor
import br.ufc.insightlab.linkedgraphast.modules.fragmentextractor.FragmentExtractor
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.{SimilarityKeywordMatcherOptimized, SimilarityKeywordMatcherOptimizedWithFilters}
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity.{JaroWinkler, PermutedSimilarity}
import br.ufc.insightlab.linkedgraphast.modules.querybuilder.SchemaSPARQLQueryBuilder
import br.ufc.insightlab.linkedgraphast.modules.vonqbe.{VonQBEFragmentExtractor, VonQBESparqlBuilder}
import br.ufc.insightlab.linkedgraphast.parser.NTripleParser
import br.ufc.insightlab.linkedgraphast.query.steinertree.SteinerTree

object Experiment extends App {

  val graph = NTripleParser.parse("src/main/resources/imdb-schema-clean.nt")

//  println(
//    graph.getLinksAsStream
//        .filter(l => l.uri.uri.contains("#domain") || l.uri.uri.contains("#range"))
//      .map(_.source.uri).distinct.mkString("\n"))

  val searches = List(
  "actor"
  )

//  val (nodes,filters) = new SimilarityKeywordMatcherOptimizedWithFilters(new PermutedSimilarity(JaroWinkler))(graph)(s)
//  println(nodes.mkString(","))
//  println(filters.mkString("\n"))
//
//  val fragment = SteinerTree(graph)(nodes.toList)
//  println(fragment.linksAsString())
//
//  println("\nSuggestions:\n\t"+FragmentExpansor(graph)(fragment).mkString("\n\t"))
//
//  val query1 = SchemaSPARQLQueryBuilder(fragment,filters,graph)
//
//  println("\n"+query1)
  val useNer = false
  if(useNer)
    Figer.init("src/main/resources/figer.conf")

  for(s <- searches){

    val fragment = new VonQBEFragmentExtractor(graph).generateFragment(s)
    val suggestions = FragmentExpansor(graph)(fragment)
    println(s"Suggestions to $s:\n${suggestions.mkString(",")}\n")

    val query2 = new VonQBESparqlBuilder(graph, useNer).generateSPARQL(s, useNer)

    println(s"SPARQL generated to search '$s' : \n\n$query2\n\n")
  }

}
