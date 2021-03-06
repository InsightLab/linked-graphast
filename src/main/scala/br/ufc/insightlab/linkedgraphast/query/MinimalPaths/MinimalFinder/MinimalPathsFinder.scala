package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.MinimalFinder

import br.ufc.insightlab.graphast.model.{Edge, Graph, Node}
import br.ufc.insightlab.linkedgraphast.query.MinimalPaths.utils
import br.ufc.insightlab.linkedgraphast.query.MinimalPaths.utils.{Path, PathEdge, PathMultipleEdge, PathSingleEdge}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.Queue


/**
  * A singleton class to calculate multiple minimum paths
  *
  * @see Graph
  * @see Node
  * @see Edge
  * @author Joao Castelo Branco
  * @version 0.2
  */

object MinimalPathsFinder extends MinimalPaths {

  /**
    * Function to mount the path represented by nodes
    *
    * @param source  the node that starts the path
    * @param target  the node ending the path
    * @param parents the dictionary containing minimal parents of nodes
    * @return a list of lists for each variation of paths, it is represented by nodes
    */

  private def buildPathNodes(source: Long, target: Long, parents: mutable.Map[Long, Set[Long]]): List[List[Long]] = {

    //base case, when the source was hit, the path ended
    if (source == target) {

      List(List(source))

    } else {

      //the recursion consists of assembling the list to each branch of the path
      parents(target).toList.flatMap { dad =>

        val paths: List[List[Long]] = buildPathNodes(source, dad, parents)

        paths.map((path: List[Long]) => path :+ target)

      }
    }
  }

  /**
    * Function to mount the edge path from the list of nodes that make up the minimum path
    *
    * @param pathNodes a list of lists for each variation of paths, it is represented by nodes
    * @param G         the graph where the query was made
    * @see Graph
    * @see Edge
    * @return a list of lists for each variation of paths, it is represented by edges
    */

  private def buildPathEdges(pathNodes: List[List[Long]], G: Graph): List[Path] = {

    var pathEdges: List[Path] = List()

    for (path <- pathNodes) {

      var track: List[PathEdge] = List()

      for (node <- 0 to path.length - 2) {

        val fromNode: Int = node + 1

        //collection of all edges that leave the node and is destined fromNode
        var candidates: List[Edge] = List()

        var candidate: List[Edge] = G.getOutEdges(path(node)).asScala.toList

        for (i <- candidate) {

          if (i.getToNodeId == path(fromNode) || i.getFromNodeId == path(fromNode)) {

            candidates = candidates ::: List(i)

          }
        }

        //selecting the lesser edges between them
        val widget: Double = candidates.minBy(_.getWeight).getWeight
        candidates = candidates.filter(_.getWeight == widget)

        //if the resulting list size is greater than 1, this excerpt presents redundancy and must be represented by PathMultipleEdge
        if (candidates.length > 1) {

          track = track ::: List(PathMultipleEdge(candidates))

        } else {

          //otherwise, it must be represented by PathSingleEdge
          track = track ::: List(PathSingleEdge(candidates.head))

        }
      }

      pathEdges = pathEdges :+ utils.Path(track)
    }

    pathEdges
  }

  /**
    * Breadth-first search modified to find all the minimal paths between source and target
    *
    * @param G      the graph where the query will be made
    * @param source the node that starts the path
    * @param target the node ending the path
    * @see Graph
    * @see Edge
    * @see Node
    * @return a list of lists for each variation of paths, it is represented by edges
    */

  def apply(G: Graph, source: Long, target: Long): List[Path] = {


    var parents: mutable.Map[Long, Set[Long]] = mutable.Map().withDefault(_ => Set[Long]())
    val nodes: Iterable[Node] = G.getNodes.asScala
    var colors: mutable.Map[Long, Boolean] = mutable.Map().withDefault(_ => false)
    var distances: mutable.Map[Long, Double] = mutable.Map().withDefault(_ => Double.PositiveInfinity)


    //initializing the search variables
    //    for (u <- nodes) {
    //      colors += (u.getId -> false)
    //      distances += (u.getId -> Double.PositiveInfinity)
    //      parents += (u.getId -> Set())
    //    }

    colors += source -> true
    distances += (source -> 0)

    val nodesQueue: mutable.PriorityQueue[(Long, Double)] = mutable.PriorityQueue((source, 0.0))(Ordering.by(-_._2))


    while (nodesQueue.nonEmpty) {
      val deq = nodesQueue.dequeue
      //      println(deq)
      val node: Long = deq._1

      if(node == target) {
//        println(s"target found with distance ${distances(target)}")
        nodesQueue.dequeueAll
      }

      //iteration over the edges that leave the node
      else for {

        edge <- G.getOutEdges(node).asScala

      } {

        val neighborId: Long =
          if (edge.isBidirectional)
            if (edge.getToNodeId == node) edge.getFromNodeId
            else edge.getToNodeId
          else edge.getToNodeId


        if (!colors(neighborId)) { //iteration over the edges that leave the node

          val widget: Double = distances(node) + edge.getWeight

          //if the weight found is less than equal to the lowest already listed
          if (widget <= distances(neighborId)) {

            //security check to prevent the target being a part of some way minimum path
            //            if (neighborId != target) {
            //              nodesQueue.enqueue((neighborId, widget))
            //            }

            //if the distance found was equal to less cataloged, one should register the minimal father of this node
            if (distances(neighborId) == widget) {

              parents += neighborId -> (parents(neighborId) + node)

              //otherwise, we have a better minimal parent and the others should be discarded
            }
            else {

              parents += neighborId -> Set(node)

              distances += neighborId -> widget

              nodesQueue.enqueue((neighborId, widget))

              colors += neighborId -> true
            }
          }
        }
      }

      //marking the node as visited
      //      colors += node -> true

    }

    //mounting the edge path
    //    buildPathEdges(buildPathNodes(source, target, parents) , G)
    buildPathNodes(source, target, parents)
      //      .map(_.map(G.getNode))
      .map(nodes => utils.Path(nodes.sliding(2, 1).map {
      case n1 :: n2 :: _ => PathSingleEdge(G.getEdge(n1, n2))
    } toList))

  }

}

