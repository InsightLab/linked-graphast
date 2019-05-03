package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.utils

/**
  * A class to assist the representation of paths in the graph
  *
  *
  * @author Joao Castelo Branco
  * @version 0.2
  */

case class Path(edges: List[PathEdge]) {
  override def toString: String = {
    edges.mkString("("," -> ",")")
  }

  def cost: Double = edges.foldLeft(0.0){
    case (c: Double, PathSingleEdge(e)) => c + e.getWeight
    case (c: Double, PathMultipleEdge(es)) => c + es.head.getWeight
  }
}
