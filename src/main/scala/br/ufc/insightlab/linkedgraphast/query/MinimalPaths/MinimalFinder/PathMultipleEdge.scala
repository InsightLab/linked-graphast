package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.MinimalFinder

import br.ufc.insightlab.graphast.model.Edge

case class PathMultipleEdge(edges: List[Edge]) extends PathEdge {


  override def toString: String = {
    var string : String =""
    for(edge <- edges){
      string = string + " " + edge.toString
    }
    string
  }

}
