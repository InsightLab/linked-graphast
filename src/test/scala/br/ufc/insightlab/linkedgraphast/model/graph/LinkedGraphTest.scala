package br.ufc.insightlab.linkedgraphast.model.graph

import br.ufc.insightlab.linkedgraphast.model.link.{Attribute, Relation}
import br.ufc.insightlab.linkedgraphast.model.node.URI
import org.scalatest.{BeforeAndAfterEach, FunSuite}

class LinkedGraphTest extends FunSuite with BeforeAndAfterEach {

  var graph: LinkedGraph = _
  val url = "http://insight.br.ufc.br/ontology/"
  val typeURI = URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")

  val rdfClass = URI("http://www.w3.org/2000/01/rdf-schema#Class")
  val person = URI(url + "Person")
  val work = URI(url + "Work")

  val person1 = URI(url + "Person/1")
  val person2 = URI(url + "Person/2")

  val work1 = URI(url + "Work/1")
  val work2 = URI(url + "Work/2")

  val r1 = Relation(person, typeURI, rdfClass)
  val r2 = Relation(work, typeURI, rdfClass)
  val r3 = Relation(person1, typeURI, person)
  val r4 = Relation(person2, typeURI, person)
  val r5 = Relation(work1, typeURI, work)
  val r6 = Relation(work2, typeURI, work)

  val r7 = Relation(person1, URI(url + "married"), person2)
  val r8 = Relation(person2, URI(url + "married"), person1)

  val r9 = Relation(person1, URI(url + "worksAt"), work1)
  val r10 = Relation(person1, URI(url + "worksAt"), work2)
  val r11 = Relation(person2, URI(url + "worksAt"), work2)

  val a1 = new Attribute(person1, URI(url + "name"), "Chuck")
  val a2 = new Attribute(person1, URI(url + "age"), "29")
  val a3 = new Attribute(person2, URI(url + "name"), "Sarah")
  val a4 = new Attribute(person2, URI(url + "age"), "30")
  val a5 = new Attribute(work1, URI(url + "name"), "BuyMore")
  val a6 = new Attribute(work2, URI(url + "name"), "CIA")

  override def beforeEach() {
    graph = new LinkedGraph

    graph.addNode(rdfClass)
    graph.addNode(person)
    graph.addNode(work)

    graph.addNode(person1)
    graph.addNode(person2)
    graph.addNode(work1)
    graph.addNode(work2)

    graph.addLink(r1)
    graph.addLink(r2)

    graph.addLink(r3)
    graph.addLink(r4)
    graph.addLink(r5)
    graph.addLink(r6)

    graph.addLink(a1)
    graph.addLink(a2)

    graph.addLink(a3)
    graph.addLink(a4)

    graph.addLink(r7)
    graph.addLink(r8)

    graph.addLink(a5)
    graph.addLink(a6)

    graph.addLink(r9)
    graph.addLink(r10)
    graph.addLink(r11)

  }

  def sameElements[A](s: Stream[A], keep: List[A]):Boolean =
    keep.forall(s.contains(_)) &&
      s.forall(keep.contains(_)) &&
      keep.size == s.size

  test("get node test"){

    assert(rdfClass == graph.getNodeByURI(rdfClass.uri))

    assert(person == graph.getNodeByURI(person.uri))
    assert(person1 == graph.getNodeByURI(person1.uri))
    assert(person2 == graph.getNodeByURI(person2.uri))

    assert(work == graph.getNodeByURI(work.uri))
    assert(work1 == graph.getNodeByURI(work1.uri))
    assert(work2 == graph.getNodeByURI(work2.uri))
  }

  test("get links using link's URI"){

    assert(sameElements(graph.getLinks(typeURI.uri), List(r1,r2,r3,r4,r5,r6)))

    assert(sameElements(graph.getLinks(url+"married"), List(r7,r8)))

    assert(sameElements(graph.getLinks(url+"worksAt"), List(r9,r10,r11)))

    assert(sameElements(graph.getLinks(url+"name"), List(a1,a3,a5,a6)))

    assert(sameElements(graph.getLinks(url+"age"), List(a2,a4)))

  }

  test("get links using source"){

    assert(sameElements(graph.getLinks(sourceURI = person.uri), List(r1)))
    assert(sameElements(graph.getLinks(sourceURI = work.uri), List(r2)))

    assert(sameElements(graph.getLinks(sourceURI = person1.uri), List(r3,r7,r9,r10,a1,a2)))

    assert(sameElements(graph.getLinks(sourceURI = person2.uri), List(r4,r8,r11,a3,a4)))

    assert(sameElements(graph.getLinks(sourceURI = work1.uri), List(r5,a5)))

    assert(sameElements(graph.getLinks(sourceURI = work2.uri), List(r6,a6)))
  }

  test("get links using target"){
    assert(sameElements(graph.getLinks(target = rdfClass.uri),List(r1,r2)))

    assert(sameElements(graph.getLinks(target = person.uri),List(r3,r4)))

    assert(sameElements(graph.getLinks(target = work.uri),List(r5,r6)))

    assert(sameElements(graph.getLinks(target = person1.uri),List(r8)))

    assert(sameElements(graph.getLinks(target = person2.uri),List(r7)))

    assert(sameElements(graph.getLinks(target = work1.uri),List(r9)))

    assert(sameElements(graph.getLinks(target = work2.uri),List(r10, r11)))

    assert(sameElements(graph.getLinks(target = "Chuck"),List(a1)))
    assert(sameElements(graph.getLinks(target = "29"),List(a2)))
    assert(sameElements(graph.getLinks(target = "Sarah"),List(a3)))
    assert(sameElements(graph.getLinks(target = "30"),List(a4)))
    assert(sameElements(graph.getLinks(target = "BuyMore"),List(a5)))
    assert(sameElements(graph.getLinks(target = "CIA"),List(a6)))
  }

  test("get links using source and link's URI"){
    assert(sameElements(graph.getLinks(sourceURI = person.uri, linkURI = typeURI.uri),List(r1)))

    assert(sameElements(graph.getLinks(sourceURI = person1.uri, linkURI = typeURI.uri),List(r3)))
    assert(sameElements(graph.getLinks(sourceURI = person1.uri, linkURI = url+"married"),List(r7)))
    assert(sameElements(graph.getLinks(sourceURI = person1.uri, linkURI = url+"worksAt"),List(r9,r10)))
    assert(sameElements(graph.getLinks(sourceURI = person1.uri, linkURI = url+"name"),List(a1)))
    assert(sameElements(graph.getLinks(sourceURI = person1.uri, linkURI = url+"age"),List(a2)))

    assert(sameElements(graph.getLinks(sourceURI = person2.uri, linkURI = typeURI.uri),List(r4)))
    assert(sameElements(graph.getLinks(sourceURI = person2.uri, linkURI = url+"married"),List(r8)))
    assert(sameElements(graph.getLinks(sourceURI = person2.uri, linkURI = url+"worksAt"),List(r11)))
    assert(sameElements(graph.getLinks(sourceURI = person2.uri, linkURI = url+"name"),List(a3)))
    assert(sameElements(graph.getLinks(sourceURI = person2.uri, linkURI = url+"age"),List(a4)))

    assert(sameElements(graph.getLinks(sourceURI = work.uri, linkURI = typeURI.uri),List(r2)))

    assert(sameElements(graph.getLinks(sourceURI = work1.uri, linkURI = typeURI.uri),List(r5)))
    assert(sameElements(graph.getLinks(sourceURI = work1.uri, linkURI = url+"name"),List(a5)))

    assert(sameElements(graph.getLinks(sourceURI = work2.uri, linkURI = typeURI.uri),List(r6)))
    assert(sameElements(graph.getLinks(sourceURI = work2.uri, linkURI = url+"name"),List(a6)))

  }

  test("get links using link's URI and target"){

    assert(sameElements(graph.getLinks(linkURI = typeURI.uri, target = rdfClass.value),List(r1,r2)))
    assert(sameElements(graph.getLinks(linkURI = typeURI.uri, target = person.value),List(r3,r4)))
    assert(sameElements(graph.getLinks(linkURI = typeURI.uri, target = work.value),List(r5,r6)))

    assert(sameElements(graph.getLinks(linkURI = url+"married", target = person1.value),List(r8)))
    assert(sameElements(graph.getLinks(linkURI = url+"married", target = person2.value),List(r7)))

    assert(sameElements(graph.getLinks(linkURI = url+"worksAt", target = work1.value),List(r9)))
    assert(sameElements(graph.getLinks(linkURI = url+"worksAt", target = work2.value),List(r10,r11)))

    assert(sameElements(graph.getLinks(linkURI = url+"name", target = "Chuck"),List(a1)))
    assert(sameElements(graph.getLinks(linkURI = url+"name", target = "Sarah"),List(a3)))
    assert(sameElements(graph.getLinks(linkURI = url+"name", target = "BuyMore"),List(a5)))
    assert(sameElements(graph.getLinks(linkURI = url+"name", target = "CIA"),List(a6)))

    assert(sameElements(graph.getLinks(linkURI = url+"age", target = "29"),List(a2)))
    assert(sameElements(graph.getLinks(linkURI = url+"age", target = "30"),List(a4)))

  }

  test("get links using source and target"){

    assert(sameElements(graph.getLinks(sourceURI = person1.uri, target = person2.uri),List(r7)))
    assert(sameElements(graph.getLinks(sourceURI = person2.uri, target = person1.uri),List(r8)))
  }

  test("contains test"){

    assert(graph.containsNode("http://www.w3.org/2000/01/rdf-schema#Class"))

    assert(graph.containsNode(url+"Person"))
    assert(graph.containsNode(url+"Person/1"))
    assert(graph.containsNode(url+"Person/2"))

    assert(graph.containsNode(url+"Work"))
    assert(graph.containsNode(url+"Work/1"))
    assert(graph.containsNode(url+"Work/2"))

  }

}
