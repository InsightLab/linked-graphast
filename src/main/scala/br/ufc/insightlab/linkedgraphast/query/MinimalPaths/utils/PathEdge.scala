package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.utils

import br.ufc.insightlab.graphast.model.Edge

/**
  * auxiliary class to mark passages with or without redundancy
  *
  * @author Joao Castelo Branco
  * @version 0.1
  */

trait PathEdge {
  def getOneEdge: Edge
}
