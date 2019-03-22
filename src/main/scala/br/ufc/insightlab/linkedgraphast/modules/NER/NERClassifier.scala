package br.ufc.insightlab.linkedgraphast.modules.NER

trait NERClassifier {

  def classify(text: String): List[(String, List[String])]

}
