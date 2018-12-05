package br.ufc.insightlab.linkedgraphast.modules.keywordmatcher

import br.ufc.insightlab.graphast.model.Node
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity.SimilarityMetric
import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.model.node.Literal
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable

class SimilarityKeywordMatcherFilteringWords(wordMetric: SimilarityMetric, literalMetric: SimilarityMetric,
                                             threshold: Double = 0.9, relationURI: String = "http://insightlab.br.ufc.br/ontology/appearsIn") extends KeywordMatcher {

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  private def getWordNodes(tokens: Seq[String], graph: LinkedGraph): Seq[org.neo4j.graphdb.Node] = {
    val mostSimilars: Array[(org.neo4j.graphdb.Node, Double)] = tokens.map(_ => (null,0.0)).toArray
    log.debug(s"Tokens: ${tokens.mkString(" ")}")
    for{
      (node, word) <- graph.getWords
      i <- tokens.indices
      token = tokens(i)
    }{
      val s = wordMetric(token,word)
      if(s >= threshold && s > mostSimilars(i)._2)
        mostSimilars(i) = (node,s)
    }
    mostSimilars.map(_._1).filterNot(null == _)

  }

  override def apply(graph: LinkedGraph)(text: String): Set[Node] = {
    val tokens = text.toLowerCase.split(" ")

    val wordNodes = getWordNodes(tokens.toSet.toSeq, graph)
    log.debug(s"${wordNodes.size} word nodes found")

    val structure = graph.getNeo4jStructure


    val subsequences: IndexedSeq[IndexedSeq[String]] =
      for(i <- 0 until tokens.size)
        yield
          for(j <- i+1 to tokens.size) yield {tokens.slice(i,j) mkString " "}

    val mostSimilars = subsequences.map(l => mutable.Seq.fill[(Node, Double)](l.size)((new Node(-1),0)))
    structure.doTx({
      for(node <- wordNodes){
        log.debug(s"Iterating over literals with word ${node.getProperty("word")}")
        val it = node.getRelationships.iterator
        while(it.hasNext){
          val relation = it.next
          val literal = structure.mapNode(relation.getEndNode).asInstanceOf[Literal]

          for{
            i <- subsequences.indices
            j <- subsequences(i).indices
          } {
            val cleanLiteral = literal.value.split("\\^\\^<").head.toLowerCase
            val s: Double = literalMetric(cleanLiteral, subsequences(i)(j))

            if (s >= threshold && mostSimilars(i)(j)._2 < s)
              mostSimilars(i)(j) = (literal, s)
          }
        }
      }
    })

    var i = 0
    var nodes = Set[Node]()
    while(i < mostSimilars.size){
      val candidates: Seq[(Node,Double)] = mostSimilars(i).takeWhile(_._1.getId != -1)

      if(candidates.nonEmpty){
        val jump = candidates.size
        i = i + jump
        nodes += candidates.last._1
      }
      else i += 1

    }
    log.debug(s"Nodes: $nodes")
    nodes
  }
}
