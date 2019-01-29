package br.ufc.insightlab.linkedgraphast.modules.keywordmatcher

import br.ufc.insightlab.graphast.model.Node
import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.modules.figer.Figer
import br.ufc.insightlab.linkedgraphast.modules.fragmentextractor.FragmentExtractor
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity.SimilarityMetric
import br.ufc.insightlab.linkedgraphast.modules.querybuilder.{MultipleSchemaSPARQLQueryBuilder, SchemaSPARQLQueryBuilder}
import br.ufc.insightlab.linkedgraphast.query.steinertree.SteinerTree

import scala.collection.mutable.ListBuffer

class NERKeywordMatcher(metric: SimilarityMetric, threshold: Double = 0.9) {
  require(threshold <= 1.0)
  require(threshold >= 0.0)

  private val matcher =
    new SimilarityKeywordMatcherOptimizedWithFilters(metric, threshold)

  Figer.init()

  private def generateCombinations
    (text: String, entities: List[(String,List[String])]): List[String] = {

    if(entities.isEmpty) List(text)
    else{
      val (entity,tags) = entities.head
      val entityFilters = entity.split(" ").map(x => s"[contains;$x]").mkString("")
      tags.flatMap(t =>
        generateCombinations(
          text.replace(entity, t+entityFilters),
          entities.tail)
      )
    }

  }

  private def getFragmentSize(fragment: LinkedGraph): Int =
    fragment.getLinksAsStream.count(l =>
      !l.uri.uri.contains("#range") && !l.uri.uri.contains("#domain")
    )

  def apply(graph: LinkedGraph)(text: String): (ListBuffer[Node],Map[Long,List[String]]) = {

    val capText = text.split(" ").map(_.capitalize).mkString(" ")
    println(s"Classifying entities for text: $capText")

    val entities = Figer.classify(capText)
    println(s"\n${entities.size} entities found:\n${entities.mkString(" ")}")

    val textsCandidates = generateCombinations(capText, entities)

    println(s"\n${textsCandidates.size} candidate texts:\n${textsCandidates.mkString("\n")}")

    val sortedTextsNodes = textsCandidates.par
      .map(text => (text,matcher(graph)(text.toLowerCase)))
      .toList.sortBy{case (_,(nodes,_)) => -nodes.distinct.size}

    val maximalNodes = sortedTextsNodes
      .takeWhile{case (_, (nodes,_)) =>
        nodes.distinct.size == sortedTextsNodes.head._2._1.distinct.size}

    println(s"\n${maximalNodes.size} maximal nodes list:\n${maximalNodes.mkString("\n")}")

    val fragments = maximalNodes.par
      .map{case (text, (nodes,filters)) =>
        (text, SteinerTree(graph)(nodes.toList), filters)}
      .toList.sortBy(x => getFragmentSize(x._2))

    println(s"\n${fragments.size} fragments retreived:\n" +
      s"${fragments.map{case (text, fragment, _) =>
        s"Text: $text\n"+fragment.linksAsString()}.mkString("\n")}")

    val minimalFragments = fragments
      .takeWhile(x =>
        getFragmentSize(x._2) == getFragmentSize(fragments.head._2))

    println(s"\n${minimalFragments.size} minimal fragments retreived:\n" +
      s"${minimalFragments.map{case (text, fragment, _) =>
        s"Text: $text\n"+fragment.linksAsString()}.mkString("\n")}")

    println(s"\nQueries generated:\n"+
      s"${minimalFragments.map{case (text, fragment, filters) =>
        s"Text: $text\n${SchemaSPARQLQueryBuilder(fragment, filters, graph)}\n\n"
      }}")

    println("\nCombined query:\n"+
      MultipleSchemaSPARQLQueryBuilder(minimalFragments.map(x => (x._2,x._3)), graph))

    (ListBuffer(), Map())
  }
}
