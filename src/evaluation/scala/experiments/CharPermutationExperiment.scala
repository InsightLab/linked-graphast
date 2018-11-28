package experiments

import experiments.helper.PermutationHelper
import org.insightlab.linkedgraphast.model.graph.LinkedGraph

class CharPermutationExperiment(t: String, graph: LinkedGraph) extends Experiment(t, graph) {

  override def preProcess: String = PermutationHelper.charPermutation(text)

  override def toString: String = "Char permutation"

}
