package br.ufc.insightlab.linkedgraphast.query.closure

import br.ufc.insightlab.graphast.model.{Edge, Node}
import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.model.link.{Link, Relation}
import br.ufc.insightlab.linkedgraphast.model.node.URI
import br.ufc.insightlab.linkedgraphast.query.MinimalPaths.MinimalFinder.MinimalPathsFinder
import br.ufc.insightlab.linkedgraphast.query.MinimalPaths.utils.{Path, PathMultipleEdge, PathSingleEdge}
import org.slf4j.{Logger, LoggerFactory}

object MinimalPathsClosure extends Closure {

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  override def apply(g: LinkedGraph)(nodes: List[Node]): LinkedGraph = {
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
            else if (l.uri.contains("rdf-schema#sub"))
              link.setWeight(10)

          case _ =>
        })


      val pairNodes = nodes
        .combinations(2)
        .map(e => (e.head.getId, e.tail.head.getId))
        //      .map(x => if(x._1 < x._2) x else (x._2,x._1))
        .filterNot(x => x._1 == x._2)

      var modifiedEdges = List[(Edge, Double)]()

      for ((n1, n2) <- pairNodes) {
        println(s"Computing path from ${g.getNode(n1)} to ${g.getNode(n2)}")
        val paths = MinimalPathsFinder(g, n1, n2)

        for (path <- paths) {
          println(path)
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

//  private def compressPath(path: Path): List[Link] = {
//    for (pair <- path.edges.sliding(2, 1)) {
//      pair match {
//        case PathMultipleEdge(edges1) :: PathMultipleEdge(edges2) :: Nil =>
//        case l1 :: Nil =>
//      }
//    }
//  }
}
