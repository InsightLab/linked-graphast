package br.ufc.insightlab

import br.ufc.insightlab.linkedgraphast.modules.figer.Figer
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.NERKeywordMatcher
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity.{JaroWinkler, PermutedSimilarity}
import br.ufc.insightlab.linkedgraphast.parser.NTripleParser
import edu.stanford.nlp.ling.CoreAnnotations.{SentencesAnnotation, TokensAnnotation}
import edu.stanford.nlp.ling.{CoreAnnotations, CoreLabel}
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.util.{CoreMap, Pair, StringUtils}
import edu.washington.cs.figer.FigerSystem
import edu.washington.cs.figer.analysis.Preprocessing

import scala.collection.JavaConverters._

object FIGERTest extends App {

//  val sentences = List(
//    "John Smit is a president of United States",
//    "Tom Hanks appears at Central Park",
//    "Brazil is the country of Football / Soccer"
//  )
//
//  for(s <- sentences){
//    println(s"Classifying sentence $s")
//    println(Figer.classify(s).mkString("\n"))
//    println("\n-------------------\n")
//  }

  val graph = NTripleParser.parse("src/main/resources/dbpedia.nt")
//  val graph = NTripleParser.parse("src/main/resources/dbpedia.nt")

  val matcher = new NERKeywordMatcher(new PermutedSimilarity(JaroWinkler))

//  val s = "tom hanks and bruce willis movies"
  val s = "John Smith' books"

  matcher(graph)(s)


}
