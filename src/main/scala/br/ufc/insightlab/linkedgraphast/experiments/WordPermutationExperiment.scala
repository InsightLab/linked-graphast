package br.ufc.insightlab.linkedgraphast.experiments

import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import experiments.helper.PermutationHelper

class WordPermutationExperiment(t: String, graph: LinkedGraph) extends Experiment(t, graph) {
  override def preProcess: String = PermutationHelper.wordPermutation(text)

  override def toString: String = "Words permutation"
}
