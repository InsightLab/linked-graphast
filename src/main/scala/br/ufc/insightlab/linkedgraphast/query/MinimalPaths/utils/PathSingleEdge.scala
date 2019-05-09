package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.utils

import br.ufc.insightlab.graphast.model.Edge

/**
  *Auxiliary class to demarcate the section without redundancy
  *
  * @author Joao Castelo Branco
  * @see Edge
  * @version 0.1
  */

case class PathSingleEdge(edge: Edge) extends PathEdge {

  override def toString: String = edge.toString


}
