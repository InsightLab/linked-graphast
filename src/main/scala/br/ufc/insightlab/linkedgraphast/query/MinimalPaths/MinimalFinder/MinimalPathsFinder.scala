package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.MinimalFinder

import br.ufc.insightlab.graphast.model.{Edge, Graph}

trait MinimalPathsFinder {
  def apply(G: Graph , source: Long , target: Long) :List[Path]
}
