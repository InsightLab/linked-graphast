package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.MinimalFinder

import br.ufc.insightlab.graphast.model.Edge

case class PathSingleEdge(edge: Edge) extends PathEdge {

  override def toString: String = {
    edge.toString
  }

}
