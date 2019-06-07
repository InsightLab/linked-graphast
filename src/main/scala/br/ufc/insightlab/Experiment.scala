package br.ufc.insightlab

import br.ufc.insightlab.linkedgraphast.modules.NER.wikifier.Wikifier
import br.ufc.insightlab.linkedgraphast.modules.vonqbe.{VonQBEFragmentExtractor, VonQBESparqlBuilder}
import br.ufc.insightlab.linkedgraphast.parser.NTripleParser
import scala.collection.JavaConverters._

object Experiment extends App {

  val graph = NTripleParser.parse("src/main/resources/dbpedia.nt")

  val searches = List(
  "Which awards did Douglas Hofstadter win?"
  )


  val useNer = true
  val ner = Wikifier


  val sortedNodes = graph
    .getNodes.asScala
    .map(n => (n,graph.getInEdges(n.getId).asScala.size))
    .toStream.sortBy(-_._2)

  println(sortedNodes.take(10).mkString("\n"))

  for(s <- searches){
    val query2 = new VonQBESparqlBuilder(graph, ner).generateSPARQL(s, useNer)
    println(s"SPARQL generated to search '$s' : \n\n$query2\n\n")
  }
}
