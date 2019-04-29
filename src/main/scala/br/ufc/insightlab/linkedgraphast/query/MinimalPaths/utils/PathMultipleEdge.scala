package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.utils

import br.ufc.insightlab.graphast.model.Edge

//class to mark a redundancy, there is a collection of the edges that are part of this redundancy
case class PathMultipleEdge(edges: List[Edge]) extends PathEdge {


  override def toString: String = edges.mkString("{ "," , "," }")

}
