package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.utils

import br.ufc.insightlab.graphast.model.Edge

/**
  *Class to mark a redundancy
  *There is a collection of the edges that are part of this redundancy
  *
  * @author Joao Castelo Branco
  * @version 0.1
  */

case class PathMultipleEdge(edges: List[Edge]) extends PathEdge {


  override def toString: String = edges.mkString("{ "," , "," }")

}
