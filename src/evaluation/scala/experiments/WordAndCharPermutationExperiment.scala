package experiments

import experiments.helper.PermutationHelper
import org.insightlab.linkedgraphast.model.graph.LinkedGraph

class WordAndCharPermutationExperiment(t: String, graph: LinkedGraph) extends Experiment(t, graph) {
  override def preProcess: String = PermutationHelper.wordAndCharPermutation(text)

  override def toString: String = "Permutation of word and char"
}
