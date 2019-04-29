package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.utils

import br.ufc.insightlab.graphast.model.Edge

case class PathMultipleEdge(edges: List[Edge]) extends PathEdge {


  override def toString: String = edges.mkString("{ "," , "," }")

}
