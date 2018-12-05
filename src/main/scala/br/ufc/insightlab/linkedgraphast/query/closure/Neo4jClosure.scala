package br.ufc.insightlab.linkedgraphast.query.closure

import br.ufc.insightlab.graphast.model.{Edge, Node}
import br.ufc.insightlab.graphast.query.shortestpath.DijkstraStrategy
import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.query.mst.Prim
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object Neo4jClosure {

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  def apply(g: LinkedGraph)(nodes: List[Node]): LinkedGraph = {

    g.getNeo4jStructure

    val graph = new LinkedGraph

    for (n <- nodes) graph.addNode(n)

    if (nodes.size == 1) graph
    else {

//      undirectedG.getInstancesDefinitions()
//        .par
//        .foreach(r => {
//          undirectedG.removeEdge(r.getId)
//          r.setWeight(Double.MaxValue)
//          undirectedG.addEdge(r)
//        }
//      )

      val pairNodes = nodes
        .combinations(2)
        .map(e => (e.head.getId, e.tail.head.getId))
        //      .map(x => if(x._1 < x._2) x else (x._2,x._1))
        .filterNot(x => x._1 == x._2)

      val dijkstra = new DijkstraStrategy(g)

      var edges = Set[Edge]()

      for ((n1, n2) <- pairNodes) {
        log.debug(s"Computing path from ${g.getNode(n1)} to ${g.getNode(n2)}")
        val path: List[Long] = dijkstra.run(n1, n2)
          .getPath(n2)
          .asScala
          .toList
          .map(_.asInstanceOf[Long])

        log.debug(s"Path found: ${path.map(g.getNode).mkString(",")}")

        var src = path.head


        for (trg <- path.tail) {
          val edge = g.getEdge(src, trg)
          g.removeEdge(edge.getId)
          edge.setWeight(0.5)
          g.addEdge(edge)

          if (!graph.containsNode(src)) {
            graph.addNode(g.getNode(src))
//            log.debug(s"Adding node $src")
          }

          if (!graph.containsNode(trg)) {
            graph.addNode(g.getNode(trg))
//            log.debug(s"Adding node $src")
          }

          g.getEdge(src, trg) match {
            case e: Edge =>
              //            println(e)
              //            println(s"Adding edge $src -> $trg")
              edges += e

            case _ =>
              val e = g.getEdge(trg, src)
              //            println(e)
              //            println(s"Adding edge $trg -> $src")
              edges += e
          }
          src = trg
        }

      }

      //    println(edges.mkString(", "))

      for (e <- edges) {
        //      println(e)
        graph.addEdge(e)
      }

      Prim(graph)(nodes.head)
    }
  }

}
