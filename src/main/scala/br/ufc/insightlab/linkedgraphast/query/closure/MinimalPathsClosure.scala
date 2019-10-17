package br.ufc.insightlab.linkedgraphast.query.closure

import br.ufc.insightlab.graphast.model.{Edge, Node}
import br.ufc.insightlab.graphast.query.shortestpath.DijkstraStrategy
import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.model.link.{Attribute, Link, Relation}
import br.ufc.insightlab.linkedgraphast.model.node.URI
import br.ufc.insightlab.linkedgraphast.modules.querybuilder.SchemaSPARQLQueryBuilder
import br.ufc.insightlab.linkedgraphast.parser.NTripleParser
import br.ufc.insightlab.linkedgraphast.query.MinimalPaths.MinimalFinder.MinimalPathsFinder
import br.ufc.insightlab.linkedgraphast.query.MinimalPaths.utils.{Path, PathEdge, PathMultipleEdge, PathSingleEdge}
import org.slf4j.{Logger, LoggerFactory}

object MinimalPathsClosure extends Closure {

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  override def apply(g: LinkedGraph)(nodes: List[Node]): LinkedGraph = {
    val graph = new LinkedGraph

    for (n <- nodes) graph.addNode(n)

    if (nodes.size == 1) graph
    else {
      ClosureHelper.adjustEdgesWeight(g)

      val pairNodes = nodes
        .combinations(2)
        .map(e => (e.head.getId, e.tail.head.getId))
        //      .map(x => if(x._1 < x._2) x else (x._2,x._1))
        .filterNot(x => x._1 == x._2)

//      var modifiedEdges = List[(Edge, Double)]()

      var varPropertiesCount = 0
      for ((n1, n2) <- pairNodes) {
//        println(s"Computing path from ${g.getNode(n1)} to ${g.getNode(n2)}")

        val paths = MinimalPathsFinder(g, n1, n2).filterNot(_.cost.toString == "Infinity")
//        if(paths.nonEmpty) {
//          println(s"${paths.size} paths found with cost: ${paths.head.cost}")
//        }

        val compressedPaths = (for (path <- paths) yield {
          val compressedPath = compressPath(path, varPropertiesCount)
//          println(s"\nPath: $path\n\nCompressed Path: $compressedPath\n--------------------")
          compressedPath
        }).distinct

        varPropertiesCount += compressedPaths.count(_.exists(_.source.uri.startsWith("?")))

//        println(compressedPaths.mkString("\n"))
        for{
          path <- compressedPaths
          link <- path
        }{
          if(!graph.containsNode(link.source.getId))
            graph.addNode(link.source)
          if(!graph.containsNode(link.target.getId))
            graph.addNode(link.target)
          if(!graph.hasLink(link))
            graph.addLink(link)
        }

      }

      graph
    }
  }

  private def isPropertyDefinition(l1: Link, l2: Link): Boolean =
    (l1, l2) match {
      case (Relation(s1, URI(p1), o1), Relation(s2, URI(p2), o2)) =>
        (
          (p1.endsWith("#range") && p2.endsWith("#domain")) ||
            (p2.endsWith("#range") && p1.endsWith("#domain"))
        ) && s1 == s2

      case _ => false

    }

  private def compressPath(path: Path, c: Int = 0): List[Link] = {
    var i = c
    var hasProcessedProperty = false
    val links = (for (pair <- path.edges.sliding(2, 1)) yield {
      pair match {
        case (p1:PathEdge) :: (p2:PathEdge):: Nil =>
          val l1 = p1.getOneEdge.asInstanceOf[Link]
          val l2 = p2.getOneEdge.asInstanceOf[Link]
          if(isPropertyDefinition(l1, l2)){
            hasProcessedProperty = true
            i += 1
            List(
              Relation(URI(s"?p${i}"),l1.uri, l1.target.asInstanceOf[URI]),
              Relation(URI(s"?p${i}"),l2.uri, l2.target.asInstanceOf[URI])
            )
          } else if(hasProcessedProperty){
            hasProcessedProperty = false
            Nil
          }  else List(l1)

        case (p:PathEdge) :: Nil =>
          if(hasProcessedProperty) Nil else List(p.getOneEdge.asInstanceOf[Link])
      }
    }).toList.flatten

    if(hasProcessedProperty) links
    else links ::: List(path.edges.last.getOneEdge.asInstanceOf[Link])
  }


  def main(args: Array[String]): Unit = {
    val graph = NTripleParser.parse("src/main/resources/dbpedia.nt")

    val fromNode = graph.getNodeByURI("artist@en")
    val toNode = graph.getNodeByURI("award@en")

    val nodes = List(fromNode, toNode)

    val fragment = MinimalPathsClosure(graph)(nodes)

    println(fragment.linksAsString())

    println("-------------------")

    println(SchemaSPARQLQueryBuilder(fragment, Map.empty, graph))


  }
}
