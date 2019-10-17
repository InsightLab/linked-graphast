package br.ufc.insightlab.linkedgraphast.query.steinertree

import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.query.closure.{DijkstraClosure, MinimalPathsClosure}
import br.ufc.insightlab.graphast.model.Node

object SteinerTree {

  def getTreeByURI(g: LinkedGraph, URIs: List[String], withMinimalPaths: Boolean = false): LinkedGraph =
    this(g, withMinimalPaths)(URIs.map(g.getNodeByURI))

  def apply(g: LinkedGraph, withMinimalPaths: Boolean = false)(nodes: List[Node]): LinkedGraph = {
    require(nodes.forall{n => g.containsNode(n.getId)},"All nodes must be on the graph")

    if(withMinimalPaths) MinimalPathsClosure(g)(nodes)
    else DijkstraClosure(g)(nodes)
  }
}
