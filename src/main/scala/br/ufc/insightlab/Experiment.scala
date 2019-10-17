package br.ufc.insightlab

import br.ufc.insightlab.linkedgraphast.modules.NER.wikifier.Wikifier
import br.ufc.insightlab.linkedgraphast.modules.vonqbe.{VonQBEFragmentExtractor, VonQBESparqlBuilder}
import br.ufc.insightlab.linkedgraphast.parser.{JenaRdfParser, NTripleParser}

object Experiment extends App {

  val graph = JenaRdfParser.parse("src/main/resources/dbpedia.nt")
//  val graph = NTripleParser.parse("/Users/lucasperes/Desktop/dbpedia-extracted-schema.nt")

  val searches = List(
  "Which pope succeeded John Paul II?"
  )

  val ner = Wikifier

  for(s <- searches){
    val querySimple = new VonQBESparqlBuilder(graph, 1, ner).generateSPARQL(s)
    println(s"Von-QBE SPARQL'$s' : \n\n$querySimple\n\n")

    val queryNer = new VonQBESparqlBuilder(graph, 1, ner).generateSPARQL(s, withNER = true, withMinimalPaths = true)
    println(s"Von-QBNER SPARQL'$s' : \n\n$queryNer\n\n")
  }
}
