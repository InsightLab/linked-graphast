package br.ufc.insightlab.linkedgraphast.model.node

import org.scalatest.FunSuite

class URITest extends FunSuite {
  test("object creation"){
    val uri = "http://insightlab.br.ufc.br/Class/"
    val classURI = "http://insightlab.br.ufc.br/Class"

    val i1 = URI(uri)
    assert(i1.uri == uri)
  }

  test("object comparision"){
    val uri = "http://insightlab.br.ufc.br/Class/"
    val classURI = "http://insightlab.br.ufc.br/Class"

    val i1 = URI(uri+"1")
    val i2 = URI(uri+"2")
    val i3 = URI(uri+"1")

    assert(i1 == i3)
    assert(i1 != i2)
    assert(i2 != i3)
  }
}
