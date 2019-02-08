package br.ufc.insightlab.linkedgraphast.experiments

import experiments.helper.PermutationHelper
import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph

class CharPermutationExperiment(t: String, graph: LinkedGraph) extends Experiment(t, graph) {

  override def preProcess: String = PermutationHelper.charPermutation(text)

  override def toString: String = "Char permutation"

}
