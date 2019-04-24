package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.MinimalFinder

import br.ufc.insightlab.graphast.model.{Edge, Graph}
import org.scalatest.{BeforeAndAfterEach, FunSuite}

class MPFinderTest extends FunSuite with BeforeAndAfterEach {

  //graphs
  var graph_multiple_different_paths : Graph = new Graph
  var graph_multiple_cross_paths : Graph = new Graph
  var graph_one_node : Graph = new Graph
  var graph_one_node_with_edge : Graph = new Graph
  var graph_with_nodes_without_edges : Graph = new Graph
  var graph_with_path_size_1 : Graph = new Graph

  override def beforeEach(): Unit = {
    graph_multiple_different_paths = new Graph
    graph_multiple_cross_paths = new Graph
    graph_one_node = new Graph
    graph_one_node_with_edge = new Graph
    graph_with_nodes_without_edges = new Graph


    //buid graph_multiple_different_paths
    for(i<-0l to 11l){
      graph_multiple_different_paths.addNode(i)
    }

    graph_multiple_different_paths.addEdge(new Edge(0,1))
    graph_multiple_different_paths.addEdge(new Edge(1,0))

    graph_multiple_different_paths.addEdge(new Edge(0,4))
    graph_multiple_different_paths.addEdge(new Edge(4,0))

    graph_multiple_different_paths.addEdge(new Edge(0,3))
    graph_multiple_different_paths.addEdge(new Edge(3,0))

    graph_multiple_different_paths.addEdge(new Edge(1,4))
    graph_multiple_different_paths.addEdge(new Edge(4,1))

    graph_multiple_different_paths.addEdge(new Edge(1,2))
    graph_multiple_different_paths.addEdge(new Edge(2,1))

    graph_multiple_different_paths.addEdge(new Edge(2,11))
    graph_multiple_different_paths.addEdge(new Edge(11,2))

    graph_multiple_different_paths.addEdge(new Edge(3,5))
    graph_multiple_different_paths.addEdge(new Edge(5,3))

    graph_multiple_different_paths.addEdge(new Edge(4,6))
    graph_multiple_different_paths.addEdge(new Edge(6,4))

    graph_multiple_different_paths.addEdge(new Edge(4,11))
    graph_multiple_different_paths.addEdge(new Edge(11,4))

    graph_multiple_different_paths.addEdge(new Edge(11,7))
    graph_multiple_different_paths.addEdge(new Edge(7,11))

    graph_multiple_different_paths.addEdge(new Edge(5,8))
    graph_multiple_different_paths.addEdge(new Edge(8,5))

    graph_multiple_different_paths.addEdge(new Edge(5,6))
    graph_multiple_different_paths.addEdge(new Edge(6,5))

    graph_multiple_different_paths.addEdge(new Edge(6,9))
    graph_multiple_different_paths.addEdge(new Edge(9,6))

    graph_multiple_different_paths.addEdge(new Edge(6,7))
    graph_multiple_different_paths.addEdge(new Edge(7,6))

    graph_multiple_different_paths.addEdge(new Edge(6,10))
    graph_multiple_different_paths.addEdge(new Edge(10,6))

    graph_multiple_different_paths.addEdge(new Edge(6,7))
    graph_multiple_different_paths.addEdge(new Edge(7,6))

    graph_multiple_different_paths.addEdge(new Edge(7,10))
    graph_multiple_different_paths.addEdge(new Edge(10,7))

    graph_multiple_different_paths.addEdge(new Edge(8,9))
    graph_multiple_different_paths.addEdge(new Edge(9,8))

    graph_multiple_different_paths.addEdge(new Edge(9,10))
    graph_multiple_different_paths.addEdge(new Edge(10,9))

    //build graph_multiple_cross_paths
    for( i <- 0l to 7l){
      graph_multiple_cross_paths.addNode(i)
    }

    graph_multiple_cross_paths.addEdge(new Edge(0,1))
    graph_multiple_cross_paths.addEdge(new Edge(1,0))

    graph_multiple_cross_paths.addEdge(new Edge(0,2))
    graph_multiple_cross_paths.addEdge(new Edge(2,0))

    graph_multiple_cross_paths.addEdge(new Edge(1,3))
    graph_multiple_cross_paths.addEdge(new Edge(3,1))

    graph_multiple_cross_paths.addEdge(new Edge(2,3))
    graph_multiple_cross_paths.addEdge(new Edge(3,2))

    graph_multiple_cross_paths.addEdge(new Edge(3,4))
    graph_multiple_cross_paths.addEdge(new Edge(4,3))

    graph_multiple_cross_paths.addEdge(new Edge(3,5))
    graph_multiple_cross_paths.addEdge(new Edge(5,3))

    graph_multiple_cross_paths.addEdge(new Edge(4,6))
    graph_multiple_cross_paths.addEdge(new Edge(6,4))

    graph_multiple_cross_paths.addEdge(new Edge(5,6))
    graph_multiple_cross_paths.addEdge(new Edge(6,5))


    //build graph_one_node
    graph_one_node.addNode(0l)

    //build graph_one_node_with_edge
    graph_one_node_with_edge.addNode(0l)
    graph_one_node_with_edge.addEdge(new Edge(0l , 0l))

    //build graph_with_nodes_without_edges
    graph_with_nodes_without_edges.addNode(0l)
    graph_with_nodes_without_edges.addNode(1l)

    //build graph_with_path_size_1
    graph_with_path_size_1.addNode(0l)
    graph_with_path_size_1.addNode(1l)

    graph_with_path_size_1.addEdge(new Edge(0l , 1l))
    graph_with_path_size_1.addEdge(new Edge(1l , 0l))

  }


  def sameElements( value1 : List[List[Long]] , value2 : List[List[Long]]) : Boolean = {

    var copy2 : List[List[Long]] = value2

    for(i <- value1){

        copy2 = copy2.filter(_ != i)

    }

    copy2.isEmpty

  }

  test("Path Verification 0 -> 10 in the graph_multiple_different_paths") {
    val path :List[List[Long]] = List( List(0l,4l,6l,10l) )

    assert(sameElements( MPFinder(graph_multiple_different_paths , 0l , 10l) , path))
  }

  test("Path Verification 0 -> 7 in the graph_multiple_different_paths") {
    val path :List[List[Long]] = List( List(0l,4l,11l,7l) , List(0l,4l,6l,7l) )

    assert(sameElements(MPFinder(graph_multiple_different_paths , 0l , 7l) , path))
  }

  test("Path Verification 2 -> 8 in the graph_multiple_different_paths") {
    val path :List[List[Long]] = List( List(2l,1l,0l,3l,5l,8l) , List(2l,11l,7l,10l,9l,8l) , List(2l,1l,4l,6l,9l,8l) , List(2l,1l,4l,6l,5l,8l) , List(2l,11l,4l,6l,5l,8l) , List(2l,11l,4l,6l,9l,8l) , List(2l,11l,7l,6l,5l,8l) , List(2l,11l,7l,6l,9l,8l))


    assert(sameElements(MPFinder(graph_multiple_different_paths , 2l , 8l) ,path))
  }
  test("Path Verification 0 -> 6 in the graph_multiple_cross_paths") {
    val path :List[List[Long]] = List( List(0l,1l,3l,4l,6l) , List(0l,1l,3l,5l,6l) , List(0l,2l,3l,5l,6l) , List(0l,2l,3l,4l,6l) )

    assert(sameElements(MPFinder(graph_multiple_cross_paths , 0l , 6l) , path))

  }

  test("Path Verification 0 -> 0 in the graph_one_node"){
    val path: List[List[Long]] = List(List(0l))

    assert(sameElements(MPFinder(graph_one_node, 0l, 0l) , path))
  }

  test("Path Verification 0 -> 0 in the graph_one_node_with_edge"){
    val path: List[List[Long]] = List(List(0l))

    assert(sameElements(MPFinder(graph_one_node_with_edge, 0l, 0l) , path))
  }

  test("Path Verification 0 -> 1 in the graph_with_nodes_without_edges"){
    val path: List[List[Long]] = List()

    assert(sameElements(MPFinder(graph_with_nodes_without_edges, 0l, 1l) , path))
  }

  test("Path Verification 0 -> 1 in the graph_with_path_size_1"){
    val path: List[List[Long]] = List(List(0l , 1l))

    assert(sameElements(MPFinder(graph_with_path_size_1, 0l, 1l) , path))
  }


}
