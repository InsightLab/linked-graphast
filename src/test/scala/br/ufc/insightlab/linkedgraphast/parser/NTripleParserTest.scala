package br.ufc.insightlab.linkedgraphast.parser

import org.scalatest.FunSuite

class NTripleParserTest extends FunSuite {
  test("reading simple example"){
    val graph = NTripleParser.parse("src/test/resources/simple_example.nt")
    assert(graph.getLinksAsStream.size == 27)
  }

}
