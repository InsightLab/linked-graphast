package br.ufc.insightlab.linkedgraphast.modules.keywordmatcher

import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.graphast.model.Node

trait KeywordMatcher {

  def apply(graph: LinkedGraph)(text: String): Set[Node]

}

