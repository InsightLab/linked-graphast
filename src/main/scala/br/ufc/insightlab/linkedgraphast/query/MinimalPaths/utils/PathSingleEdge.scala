package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.utils

import br.ufc.insightlab.graphast.model.Edge

case class PathSingleEdge(edge: Edge) extends PathEdge {

  override def toString: String = edge.toString


}
