package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.MinimalFinder

import scala.collection.JavaConverters._
import br.ufc.insightlab.graphast.model.{Edge, Graph, Node}
import br.ufc.insightlab.linkedgraphast.query.MinimalPaths.utils
import br.ufc.insightlab.linkedgraphast.query.MinimalPaths.utils.{Path, PathEdge, PathMultipleEdge, PathSingleEdge}

object MinimalPathsFinder extends MinimalPaths {
  //function to mount the path represented by nodes
  private def buildPathNodes(source: Long, target: Long, parents: Map[Long, Set[Long]]): List[List[Long]] = {
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
  //function to mount the edge path from the list of nodes that make up the minimum path
  private def buildPathEdges(pathNodes : List[List[Long]] , G : Graph) : List[Path] = {

    var pathEdges : List[Path]=  List()

    for(path <- pathNodes){

      var track : List[PathEdge] = List()

      for(node <- 0 to path.length-2){
        val fromNode : Int = node + 1

        //collection of all edges that leave the node and is destined fromNode
        var candidates : List[Edge] = G.getOutEdges(path(node)).asScala.toList.filter(_.getToNodeId == path(fromNode))

        //selecting the lesser edges between them
        val widget :Double = candidates.minBy(_.getWeight).getWeight
        candidates = candidates.filter(_.getWeight == widget)

        //if the resulting list size is greater than 1, this excerpt presents redundancy and must be represented by PathMultipleEdge
        if(candidates.length >1){

          track = track ::: List(  PathMultipleEdge(candidates) )
        }else{

          //otherwise, it must be represented by PathSingleEdge
          track = track ::: List( PathSingleEdge(candidates(0)))
        }



      }

      pathEdges = pathEdges :+ utils.Path( track )
    }

    pathEdges
  }


  //Breadth-first search modified to find all the minimal paths between source and target
  def apply(G: Graph, source: Long, target: Long): List[Path] = {


    var parents: Map[Long, Set[Long]] = Map()
    val nodes: Iterable[Node] = G.getNodes.asScala
    var colors: Map[Long, Boolean] = Map()
    var distances: Map[Long, Double] = Map()


    //initializing the search variables
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

      //iteration over the edges that leave the node
      for {
        edge <- G.getOutEdges(dad).asScala

        fromNodeId: Long = edge.getToNodeId
      } {

        //iteration over the edges that leave the node
        if (!colors(fromNodeId)) {

          val widget: Double = distances(dad) + edge.getWeight

          //if the weight found is less than equal to the lowest already listed
          if (widget <= distances(fromNodeId)) {

            //security check to prevent the target being a part of some way minimum path
            if (fromNodeId != target) {

              NextNodesId += fromNodeId


            }

            //if the distance found was equal to less cataloged, one should register the minimal father of this node
            if(distances(fromNodeId) == widget){


              parents += fromNodeId -> (parents(fromNodeId)+dad)

              //otherwise, we have a better minimal parent and the others should be discarded
            }else{

              parents += fromNodeId -> Set(dad)

              distances += fromNodeId -> widget
            }

          }

        }
      }
      //marking the node as visited
      colors += dad -> true
    }

    //mounting the edge path
    buildPathEdges(buildPathNodes(source, target, parents) , G)



  }
  
}

