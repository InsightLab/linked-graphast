package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.MinimalFinder

import java.lang
import java.nio.file.Paths
import scala.collection.JavaConverters._
import br.ufc.insightlab.graphast.model.{Edge, Graph, Node}
import javafx.scene.Parent
import nu.xom.Nodes

object MPFinder {

  def insert(parents: Map[Long, Set[Long]] ,distances: Map[Long , Double] ,dad:Long , son:Long, widget:Double ) : Map[Long , Set[Long]] ={
    if(distances(son) == widget){
      parents(son) += dad
    }
    if(widget<distances(son)){
      parents(son).empty
      parents(son) += dad
    }

    parents
  }

  def apply(G: Graph , source: Long , target: Long) :List[Paths] ={

    var parents: Map[Long, Set[Long]] = Map()
    var nodes : Iterable[Node] = G.getNodes.asScala
    var colors: Map[Long , Boolean] = Map()
    var distances : Map[Long , Double] = Map()


    for(u<-nodes){
      colors += (u.getId -> false)
      distances += (u.getId-> Double.PositiveInfinity)
      parents += (u.getId -> List)
    }

    colors(source) = true
    distances(source) = 0

    var NextNodesId:List[Long] = List(source)

    while( NextNodesId.nonEmpty){

      val dad : Long =  NextNodesId.last
      NextNodesId =  NextNodesId.init

      for{
        edge <- G.getOutEdges(dad).asScala
        fromNodeId:Long = edge.getFromNodeId
      }{
        if(!colors(fromNodeId)){

          val widget: Double = distances(dad) + edge.getWeight()
          if(widget <= distances(fromNodeId)){

            if (fromNodeId != target){
              NextNodesId :+ fromNodeId
            }

            parents = insert(parents,distances, dad ,fromNodeId , widget)
            distances(fromNodeId) = widget

          }
        }
      }
      colors(dad) = true
    }


  }


}
