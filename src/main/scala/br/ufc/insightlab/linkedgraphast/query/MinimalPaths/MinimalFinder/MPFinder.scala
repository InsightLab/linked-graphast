package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.MinimalFinder

import scala.collection.JavaConverters._
import br.ufc.insightlab.graphast.model.{Edge, Graph, Node}

object MPFinder extends MinimalPathsFinder {

  private def buildPath(source: Long, target: Long, parents: Map[Long, Set[Long]]): List[List[Long]] = {

    if (source == target) {
      List(List(source))
    } else {

      parents(target).toList.flatMap { dad =>
        val paths: List[List[Long]] = buildPath(source, dad, parents)
        paths.map((path: List[Long]) => path :+ target)

      }
    }
  }



  def apply(G: Graph, source: Long, target: Long): List[List[Long]] = {

    var parents: Map[Long, Set[Long]] = Map()
    val nodes: Iterable[Node] = G.getNodes.asScala
    var colors: Map[Long, Boolean] = Map()
    var distances: Map[Long, Double] = Map()


    for (u <- nodes) {
      colors += (u.getId -> false)
      distances += (u.getId -> Double.PositiveInfinity)
      parents += (u.getId -> Set())
    }

    colors += source -> true
    distances += (source -> 0)

    var NextNodesId: Set[Long] = Set(source)

    while (NextNodesId.nonEmpty) {

      val dad: Long = NextNodesId.head
      NextNodesId = NextNodesId - NextNodesId.head

      for {
        edge <- G.getOutEdges(dad).asScala

        fromNodeId: Long = edge.getToNodeId
      } {

        if (!colors(fromNodeId)) {

          val widget: Double = distances(dad) + edge.getWeight

          if (widget <= distances(fromNodeId)) {

            if (fromNodeId != target) {

              NextNodesId += fromNodeId


            }

            if(distances(fromNodeId) == widget){


              parents += fromNodeId -> (parents(fromNodeId)+dad)

            }else{

              parents += fromNodeId -> Set(dad)

              distances += fromNodeId -> widget
            }

          }

        }
      }
      colors += dad -> true
    }

    buildPath(source, target, parents)

  }


}

