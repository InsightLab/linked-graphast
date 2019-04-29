package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.MinimalFinder

import scala.collection.JavaConverters._
import br.ufc.insightlab.graphast.model.{Edge, Graph, Node}

object MPFinder extends MinimalPathsFinder {

  private def buildPathNodes(source: Long, target: Long, parents: Map[Long, Set[Long]]): List[List[Long]] = {

    if (source == target) {
      List(List(source))
    } else {

      parents(target).toList.flatMap { dad =>
        val paths: List[List[Long]] = buildPathNodes(source, dad, parents)
        paths.map((path: List[Long]) => path :+ target)

      }
    }
  }

  private def buildPathEdges(pathNodes : List[List[Long]] , G : Graph) : List[Path] = {
    var pathEdges : List[Path]=  List()

    for(path <- pathNodes){

      var track : List[PathEdge] = List()

      for(node <- 0 to path.length-2){
        val fromNode : Int = node + 1

        var candidates : List[Edge] = G.getOutEdges(path(node)).asScala.toList.filter(_.getToNodeId == path(fromNode))
        val widget :Double = candidates.minBy(_.getWeight).getWeight

        candidates = candidates.filter(_.getWeight == widget)

        if(candidates.length >1){

          track = track ::: List( new PathMultipleEdge(candidates) )
        }else{

          track = track ::: List(new PathSingleEdge(candidates(0)))
        }



      }

      pathEdges = pathEdges :+ Path( track )
    }

    pathEdges
  }


  def apply(G: Graph, source: Long, target: Long): List[Path] = {

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


    buildPathEdges(buildPathNodes(source, target, parents) , G)



  }
  
}

