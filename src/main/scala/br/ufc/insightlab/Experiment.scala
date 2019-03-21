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

//  val graph = NTripleParser.parse("src/main/resources/imdb-schema-clean.nt")
  val graph = NTripleParser.parse("/home/lucaspg/Documents/Repositories/spi-von-qbner/BO.nt")

  val fragment = FragmentExtractor(graph, JaroWinkler)("adf loc per bo")

  println(fragment.toNTriple)
}
