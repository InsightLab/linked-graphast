package br.ufc.insightlab.linkedgraphast.query.closure

import scala.collection.JavaConverters._
import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.model.link.{Attribute, Relation}
import br.ufc.insightlab.linkedgraphast.model.node.{Literal, URI}
import br.ufc.insightlab.linkedgraphast.query.mst.Prim
import br.ufc.insightlab.graphast.model.{Edge, Node}
import br.ufc.insightlab.graphast.query.shortestpath.DijkstraStrategy
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Success, Try}

object Closure {

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  def apply(g: LinkedGraph)(nodes: List[Node]): LinkedGraph = {
    val graph = new LinkedGraph

    for (n <- nodes) graph.addNode(n)

    if (nodes.size == 1) graph
    else {
      g.getLinksAsStream
        .foreach(link => link match {
          case Relation(_, l, t) =>
            if ((l.uri.endsWith("#type") && (t.uri.endsWith("#Class") || t.uri.endsWith("Property"))) ||
              (l.uri.endsWith("#range") && t.uri.contains("XMLSchema#")))
              link.setWeight(100)
            else if(l.uri.contains("rdf-schema#sub"))
              link.setWeight(10)

          case _ =>
        })


      val pairNodes = nodes
        .combinations(2)
        .map(e => (e.head.getId, e.tail.head.getId))
        //      .map(x => if(x._1 < x._2) x else (x._2,x._1))
        .filterNot(x => x._1 == x._2)

      val dijkstra = new DijkstraStrategy(g)

      var modifiedEdges = List[(Edge,Double)]()

      var edges = Set[Edge]()

      for ((n1, n2) <- pairNodes) {
        log.debug(s"Computing path from ${g.getNode(n1)} to ${g.getNode(n2)}")
        val vector = dijkstra.run(n1, n2)
        val path: List[Long] = Try(vector
          .getPath(n2)
          .asScala
          .toList
          .map(_.asInstanceOf[Long])) match {

          case Success(p) =>
            log.debug(s"Path found with cost ${vector.getDistance(n2)}: ${p.map(g.getNode).mkString(",")}")
            p
          case _ =>
            log.debug("Path not found")
            List[Long](-1)
        }



        var src = path.head


        for (trg <- path.tail) {
          val edge = g.getEdge(src, trg)
//          println(edge)

          if(edge.getWeight >= 1) {
            modifiedEdges :+= (edge, edge.getWeight)
            edge.setWeight(0.5)
          }

          if (!graph.containsNode(src)) {
            val n = g.getNode(src) match {
              case URI(uri) => URI(uri)
              case Literal(v) => Literal(v)
            }
            graph.addNode(n)
            //            log.debug(s"Adding node $src")
          }

          if (!graph.containsNode(trg)) {
            val n = g.getNode(trg) match {
              case URI(uri) => URI(uri)
              case Literal(v) => Literal(v)
            }
            graph.addNode(n)
            //            log.debug(s"Adding node $src")
          }

          g.getEdge(src, trg) match {
            case e: Edge =>
              edges += e

            case _ =>
              val e = g.getEdge(trg, src)
              edges += e
          }
          src = trg
        }

      }


      for (e <- edges) {
        val l = e match {
          case Attribute(s,p,o) => Attribute(s,p,o)
          case Relation(s,p,o) => Relation(s,p,o)
        }
        l.setId(e.getId)
        graph.addEdge(l)
      }
      modifiedEdges.foreach(x => x._1.setWeight(x._2))

      if(nodes.nonEmpty)
        Prim(graph)(nodes.head)
      else
        graph
    }
  }

}

