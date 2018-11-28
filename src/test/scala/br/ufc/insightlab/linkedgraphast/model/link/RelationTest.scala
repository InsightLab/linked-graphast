package br.ufc.insightlab.linkedgraphast.model.link

import br.ufc.insightlab.linkedgraphast.model.node.URI
import org.scalatest.FunSuite

class RelationTest extends FunSuite {

  val baseURI = "http://insightlab.br.ufc.br/"

  test("object creation"){

    val relationURI = URI(baseURI+"Attribute")
    val classURI = baseURI+"Class"
    val c1 = URI(classURI+"/1")
    val c2 = URI(classURI+"/2")

    val p = Relation(c1,relationURI,c2)

    assert(p.uri == relationURI)
    assert(p.source == c1)
    assert(p.target == c2)

  }
}
