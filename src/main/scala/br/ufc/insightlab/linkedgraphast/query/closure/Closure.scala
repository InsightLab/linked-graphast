package br.ufc.insightlab.linkedgraphast.query.closure

import br.ufc.insightlab.graphast.model.Node
import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph

trait Closure {

  def apply(g: LinkedGraph)(nodes: List[Node]): LinkedGraph

}
