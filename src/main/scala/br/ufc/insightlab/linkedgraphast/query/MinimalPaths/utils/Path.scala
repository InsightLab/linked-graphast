package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.utils

//auxiliary class to make paths
case class Path(edges: List[PathEdge]) {
  override def toString: String = {
    edges.mkString("("," -> ",")")
  }
}
