package br.ufc.insightlab

import br.ufc.insightlab.linkedgraphast.modules.figer.Figer
import br.ufc.insightlab.linkedgraphast.modules.fragmentexpansor.FragmentExpansor
import br.ufc.insightlab.linkedgraphast.modules.fragmentextractor.FragmentExtractor
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.{SimilarityKeywordMatcherOptimized, SimilarityKeywordMatcherOptimizedWithFilters}
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity.{JaroWinkler, PermutedSimilarity}
import br.ufc.insightlab.linkedgraphast.modules.querybuilder.SchemaSPARQLQueryBuilder
import br.ufc.insightlab.linkedgraphast.modules.vonqbe.VonQBESparqlBuilder
import br.ufc.insightlab.linkedgraphast.parser.NTripleParser
import br.ufc.insightlab.linkedgraphast.query.steinertree.SteinerTree

object Experiment extends App {

  val graph = NTripleParser.parse("src/main/resources/imdb-schema-clean.nt")

//  println(
//    graph.getLinksAsStream
//        .filter(l => l.uri.uri.contains("#domain") || l.uri.uri.contains("#range"))
//      .map(_.source.uri).distinct.mkString("\n"))

  val searches = List(
//    "tv series and their titles"
//    ,"title of movies and their actors birth name"
//    ,"company name of movies and their titles"
//    ,"title of movies on America and their budget"
//    ,"title and company name of movies from eastern asia"
//    ,

//    "actor birth name movie title[contains;die hard]"
//    ,"title of movies with actor birth name[=;hanks, tom]"
//    ,"title of movies from company name[=;Nintendo]"
//    ,"title of movies with production start year[>=;2000][<=;2010]"
//    ,"title and budget of movies with actor with birth name[=;willis, bruce] and production start year[>;2000]"
//    ,
    "tom hanks movies"
//    "actor[contains;Tom][contains;Hanks] Movies"
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
  val useNer = true
  if(useNer)
    Figer.init("src/main/resources/figer.conf")

  for(s <- searches){
    val query2 = new VonQBESparqlBuilder(graph, useNer).generateSPARQL(s, useNer)

    println(s"SPARQL generated to search '$s' : \n\n$query2\n\n")
  }

}
