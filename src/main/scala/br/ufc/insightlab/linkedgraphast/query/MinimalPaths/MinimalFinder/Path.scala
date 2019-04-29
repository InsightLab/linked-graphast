package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.MinimalFinder

case class Path(edges: List[PathEdge]) {
  override def toString: String = {
    edges.toString
  }
}
