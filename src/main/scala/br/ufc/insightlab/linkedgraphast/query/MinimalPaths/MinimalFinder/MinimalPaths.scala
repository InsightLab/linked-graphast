package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.MinimalFinder

import br.ufc.insightlab.graphast.model.{Edge, Graph}
import br.ufc.insightlab.linkedgraphast.query.MinimalPaths.utils.Path
/**
  * trait to make the method extensible
  *
  *
  * @author Joao Castelo Branco
  * @version 0.1
  */

trait MinimalPaths {
  def apply(G: Graph , source: Long , target: Long) :List[Path]
}
