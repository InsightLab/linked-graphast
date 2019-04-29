package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.utils

import br.ufc.insightlab.graphast.model.Edge

//auxiliary class to demarcate the section without redundancy, this edge
case class PathSingleEdge(edge: Edge) extends PathEdge {

  override def toString: String = edge.toString


}
