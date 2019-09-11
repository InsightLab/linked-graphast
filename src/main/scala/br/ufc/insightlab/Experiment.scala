package br.ufc.insightlab

import br.ufc.insightlab.linkedgraphast.modules.NER.wikifier.Wikifier
import br.ufc.insightlab.linkedgraphast.modules.vonqbe.{VonQBEFragmentExtractor, VonQBESparqlBuilder}
import br.ufc.insightlab.linkedgraphast.parser.{JenaRdfParser, NTripleParser}

object Experiment extends App {

//  val graph = JenaRdfParser.parse("src/main/resources/dbpedia.nt")
  val graph = NTripleParser.parse("/Users/lucasperes/Desktop/dbpedia-extracted-schema.nt")

  val searches = List(
  "Which ships were called after Benjamin Franklin?"
  )

  val useNer = true
  val ner = Wikifier

  for(s <- searches){
    val query2 = new VonQBESparqlBuilder(graph, ner).generateSPARQL(s, withNER = useNer, withMinimalPaths = true)
    println(s"SPARQL generated to search '$s' : \n\n$query2\n\n")
  }
}
