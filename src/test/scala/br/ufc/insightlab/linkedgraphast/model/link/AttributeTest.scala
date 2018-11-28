package br.ufc.insightlab.linkedgraphast.model.link

import br.ufc.insightlab.linkedgraphast.model.node.{Literal, URI}
import org.scalatest.FunSuite

class AttributeTest extends FunSuite {

  val baseURI = "http://insightlab.br.ufc.br/"

  test("object creation"){

    val attributeURI = URI(baseURI+"Attribute")
    val c1 = URI(baseURI+"Class/1")

    val p = new Attribute(c1,attributeURI,"literal")

    assert(p.uri == attributeURI)
    assert(p.source == c1)
    assert(p.target == Literal("literal"))

  }
}
