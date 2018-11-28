package br.ufc.insightlab.linkedgraphast.query.steinertree

import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.query.closure.Closure
import org.insightlab.graphast.model.Node

object SteinerTree {

  def getTreeByURI(g: LinkedGraph, URIs: List[String]): LinkedGraph =
    this(g)(URIs.map(g.getNodeByURI))

  def apply(g: LinkedGraph)(nodes: List[Node]): LinkedGraph = {
    require(nodes.forall{n => g.containsNode(n.getId)},"All nodes must be on the graph")

    Closure(g)(nodes)
  }
}
