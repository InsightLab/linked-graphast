package experiments

import experiments.helper.PermutationHelper
import org.insightlab.linkedgraphast.model.graph.LinkedGraph

class WordPermutationExperiment(t: String, graph: LinkedGraph) extends Experiment(t, graph) {
  override def preProcess: String = PermutationHelper.wordPermutation(text)

  override def toString: String = "Words permutation"
}
