package br.ufc.insightlab.linkedgraphast.experiments

import experiments.helper.PermutationHelper
import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph

class WordAndCharPermutationExperiment(t: String, graph: LinkedGraph) extends Experiment(t, graph) {
  override def preProcess: String = PermutationHelper.wordAndCharPermutation(text)

  override def toString: String = "Permutation of word and char"
}
