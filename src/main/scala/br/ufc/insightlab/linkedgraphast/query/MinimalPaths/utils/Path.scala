package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.utils

case class Path(edges: List[PathEdge]) {
  override def toString: String = {
    edges.mkString("("," -> ",")")
  }
}
