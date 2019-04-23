package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.MinimalFinder

import br.ufc.insightlab.graphast.model.{Edge, Graph}
import org.scalatest.{BeforeAndAfterEach, FunSuite}

class MPFinderTest extends FunSuite with BeforeAndAfterEach {
  var graph_one : Graph = new Graph
  var graph_two : Graph = new Graph

  override def beforeEach(): Unit = {
    graph_one = new Graph
    graph_two = new Graph

    //buid graph_one
    for(i<-0l to 11l){
      graph_one.addNode(i)
    }

    graph_one.addEdge(new Edge(0,1))
    graph_one.addEdge(new Edge(1,0))

    graph_one.addEdge(new Edge(0,4))
    graph_one.addEdge(new Edge(4,0))

    graph_one.addEdge(new Edge(0,3))
    graph_one.addEdge(new Edge(3,0))

    graph_one.addEdge(new Edge(1,4))
    graph_one.addEdge(new Edge(4,1))

    graph_one.addEdge(new Edge(1,2))
    graph_one.addEdge(new Edge(2,1))

    graph_one.addEdge(new Edge(2,11))
    graph_one.addEdge(new Edge(11,2))

    graph_one.addEdge(new Edge(3,5))
    graph_one.addEdge(new Edge(5,3))

    graph_one.addEdge(new Edge(4,6))
    graph_one.addEdge(new Edge(6,4))

    graph_one.addEdge(new Edge(4,11))
    graph_one.addEdge(new Edge(11,4))

    graph_one.addEdge(new Edge(11,7))
    graph_one.addEdge(new Edge(7,11))

    graph_one.addEdge(new Edge(5,8))
    graph_one.addEdge(new Edge(8,5))

    graph_one.addEdge(new Edge(5,6))
    graph_one.addEdge(new Edge(6,5))

    graph_one.addEdge(new Edge(6,9))
    graph_one.addEdge(new Edge(9,6))

    graph_one.addEdge(new Edge(6,7))
    graph_one.addEdge(new Edge(7,6))

    graph_one.addEdge(new Edge(6,10))
    graph_one.addEdge(new Edge(10,6))

    graph_one.addEdge(new Edge(6,7))
    graph_one.addEdge(new Edge(7,6))

    graph_one.addEdge(new Edge(7,10))
    graph_one.addEdge(new Edge(10,7))

    graph_one.addEdge(new Edge(8,9))
    graph_one.addEdge(new Edge(9,8))

    graph_one.addEdge(new Edge(9,10))
    graph_one.addEdge(new Edge(10,9))

    //build graph_two
    for( i <- 0l to 7l){
      graph_two.addNode(i)
    }

    graph_two.addEdge(new Edge(0,1))
    graph_two.addEdge(new Edge(1,0))

    graph_two.addEdge(new Edge(0,2))
    graph_two.addEdge(new Edge(2,0))

    graph_two.addEdge(new Edge(1,3))
    graph_two.addEdge(new Edge(3,1))

    graph_two.addEdge(new Edge(2,3))
    graph_two.addEdge(new Edge(3,2))

    graph_two.addEdge(new Edge(3,4))
    graph_two.addEdge(new Edge(4,3))

    graph_two.addEdge(new Edge(3,5))
    graph_two.addEdge(new Edge(5,3))

    graph_two.addEdge(new Edge(4,6))
    graph_two.addEdge(new Edge(6,4))

    graph_two.addEdge(new Edge(5,6))
    graph_two.addEdge(new Edge(6,5))


  }

  def sameElements[A](s: Stream[A], keep: List[A]):Boolean =
    keep.forall(s.contains(_)) &&
      s.forall(keep.contains(_)) &&
      keep.size == s.size
  def equal( value1 : List[List[Long]] , value2 : List[List[Long]]) : Boolean = {
    for(i <- value1){
      if( !value2.contains(i)){
        return false
      }
    }
    true
  }

  test("Path Verification 0 -> 10 in the graph_one") {
    val path :List[List[Long]] = List( List(0l,4l,6l,10l) )

    assert(equal( MPFinder(graph_one , 0l , 10l) , path))
  }

  test("Path Verification 0 -> 7 in the graph_one") {
    val path :List[List[Long]] = List( List(0l,4l,11l,7l) , List(0l,4l,6l,7l) )

    assert(equal(MPFinder(graph_one , 0l , 7l) , path))
  }

  test("Path Verification 2 -> 8 in the graph_one") {
    val path :List[List[Long]] = List( List(2l,1l,0l,3l,5l,8l) , List(2l,11l,7l,10l,9l,8l) , List(2l,1l,4l,6l,9l,8l) , List(2l,1l,4l,6l,5l,8l) , List(2l,11l,4l,6l,5l,8l) , List(2l,11l,4l,6l,9l,8l) , List(2l,11l,7l,6l,5l,8l) , List(2l,11l,7l,6l,9l,8l))


    assert(equal(MPFinder(graph_one , 2l , 8l) ,path))
  }
  test("Path Verification 0 -> 6 in the graph_two") {
    val path :List[List[Long]] = List( List(0l,1l,3l,4l,6l) , List(0l,1l,3l,5l,6l) , List(0l,2l,3l,5l,6l) , List(0l,2l,3l,4l,6l) )

    assert(equal(MPFinder(graph_two , 0l , 6l) , path))

  }


}
