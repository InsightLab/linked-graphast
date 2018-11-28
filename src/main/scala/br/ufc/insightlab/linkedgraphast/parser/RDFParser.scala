package br.ufc.insightlab.linkedgraphast.parser

import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph

trait RDFParser {
  def parse(filePath: String, graph: LinkedGraph = new LinkedGraph): LinkedGraph

  def cleanURI(uri: String): String =
    uri.reverse.takeWhile(c => c != '/' && c != '#').reverse
}
