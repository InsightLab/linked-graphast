package br.ufc.insightlab.linkedgraphast.query.MinimalPaths.MinimalFinder

import br.ufc.insightlab.graphast.model.{Edge, Graph}
import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.parser.NTripleParser
import br.ufc.insightlab.linkedgraphast.query.MinimalPaths.utils.{Path, PathMultipleEdge, PathSingleEdge}
import org.scalatest.{BeforeAndAfterEach, FunSuite}

class MinimalPathsFinderTest extends FunSuite with BeforeAndAfterEach {

  //graphs
  var graph_multiple_different_paths: Graph = new Graph
  var graph_multiple_cross_paths: Graph = new Graph
  var graph_one_node: Graph = new Graph
  var graph_one_node_with_edge: Graph = new Graph
  var graph_with_nodes_without_edges: Graph = new Graph
  var graph_with_path_size_1: Graph = new Graph
  var graph_with_redundance: Graph = new Graph


  override def beforeEach(): Unit = {
    graph_multiple_different_paths = new Graph
    graph_multiple_cross_paths = new Graph
    graph_one_node = new Graph
    graph_one_node_with_edge = new Graph
    graph_with_nodes_without_edges = new Graph
    graph_with_path_size_1 = new Graph
    graph_with_redundance = new Graph


    //buid graph_multiple_different_paths
    for (i <- 0l to 11l) {
      graph_multiple_different_paths.addNode(i)
    }

    graph_multiple_different_paths.addEdge(new Edge(0, 1))
    graph_multiple_different_paths.addEdge(new Edge(1, 0))

    graph_multiple_different_paths.addEdge(new Edge(0, 4))
    graph_multiple_different_paths.addEdge(new Edge(4, 0))

    graph_multiple_different_paths.addEdge(new Edge(0, 3))
    graph_multiple_different_paths.addEdge(new Edge(3, 0))

    graph_multiple_different_paths.addEdge(new Edge(1, 4))
    graph_multiple_different_paths.addEdge(new Edge(4, 1))

    graph_multiple_different_paths.addEdge(new Edge(1, 2))
    graph_multiple_different_paths.addEdge(new Edge(2, 1))

    graph_multiple_different_paths.addEdge(new Edge(2, 11))
    graph_multiple_different_paths.addEdge(new Edge(11, 2))

    graph_multiple_different_paths.addEdge(new Edge(3, 5))
    graph_multiple_different_paths.addEdge(new Edge(5, 3))

    graph_multiple_different_paths.addEdge(new Edge(4, 6))
    graph_multiple_different_paths.addEdge(new Edge(6, 4))

    graph_multiple_different_paths.addEdge(new Edge(4, 11))
    graph_multiple_different_paths.addEdge(new Edge(11, 4))

    graph_multiple_different_paths.addEdge(new Edge(11, 7))
    graph_multiple_different_paths.addEdge(new Edge(7, 11))

    graph_multiple_different_paths.addEdge(new Edge(5, 8))
    graph_multiple_different_paths.addEdge(new Edge(8, 5))

    graph_multiple_different_paths.addEdge(new Edge(5, 6))
    graph_multiple_different_paths.addEdge(new Edge(6, 5))

    graph_multiple_different_paths.addEdge(new Edge(6, 9))
    graph_multiple_different_paths.addEdge(new Edge(9, 6))

    graph_multiple_different_paths.addEdge(new Edge(6, 7))
    graph_multiple_different_paths.addEdge(new Edge(7, 6))

    graph_multiple_different_paths.addEdge(new Edge(6, 10))
    graph_multiple_different_paths.addEdge(new Edge(10, 6))

    //graph_multiple_different_paths.addEdge(new Edge(6, 7))
    //graph_multiple_different_paths.addEdge(new Edge(7, 6))

    graph_multiple_different_paths.addEdge(new Edge(7, 10))
    graph_multiple_different_paths.addEdge(new Edge(10, 7))

    graph_multiple_different_paths.addEdge(new Edge(8, 9))
    graph_multiple_different_paths.addEdge(new Edge(9, 8))

    graph_multiple_different_paths.addEdge(new Edge(9, 10))
    graph_multiple_different_paths.addEdge(new Edge(10, 9))

    //build graph_multiple_cross_paths
    for (i <- 0l to 7l) {
      graph_multiple_cross_paths.addNode(i)
    }

    graph_multiple_cross_paths.addEdge(new Edge(0, 1))
    graph_multiple_cross_paths.addEdge(new Edge(1, 0))

    graph_multiple_cross_paths.addEdge(new Edge(0, 2))
    graph_multiple_cross_paths.addEdge(new Edge(2, 0))

    graph_multiple_cross_paths.addEdge(new Edge(1, 3))
    graph_multiple_cross_paths.addEdge(new Edge(3, 1))

    graph_multiple_cross_paths.addEdge(new Edge(2, 3))
    graph_multiple_cross_paths.addEdge(new Edge(3, 2))

    graph_multiple_cross_paths.addEdge(new Edge(3, 4))
    graph_multiple_cross_paths.addEdge(new Edge(4, 3))

    graph_multiple_cross_paths.addEdge(new Edge(3, 5))
    graph_multiple_cross_paths.addEdge(new Edge(5, 3))

    graph_multiple_cross_paths.addEdge(new Edge(4, 6))
    graph_multiple_cross_paths.addEdge(new Edge(6, 4))

    graph_multiple_cross_paths.addEdge(new Edge(5, 6))
    graph_multiple_cross_paths.addEdge(new Edge(6, 5))


    //build graph_one_node
    graph_one_node.addNode(0l)

    //build graph_one_node_with_edge
    graph_one_node_with_edge.addNode(0l)
    graph_one_node_with_edge.addEdge(new Edge(0l, 0l))

    //build graph_with_nodes_without_edges
    graph_with_nodes_without_edges.addNode(0l)
    graph_with_nodes_without_edges.addNode(1l)

    //build graph_with_path_size_1
    graph_with_path_size_1.addNode(0l)
    graph_with_path_size_1.addNode(1l)

    graph_with_path_size_1.addEdge(new Edge(0l, 1l))
    graph_with_path_size_1.addEdge(new Edge(1l, 0l))

    //buid graph_multiple_different_paths
    for (i <- 0l to 11l) {
      graph_multiple_different_paths.addNode(i)
    }

    //buid graph_multiple_different_paths
    for (i <- 0l to 4l) {
      graph_with_redundance.addNode(i)
    }

    graph_with_redundance.addEdge(new Edge(0,1))
    graph_with_redundance.addEdge(new Edge(0,1))

    graph_with_redundance.addEdge(new Edge(1,2))
    graph_with_redundance.addEdge(new Edge(1,2))

    graph_with_redundance.addEdge(new Edge(1,3))
    graph_with_redundance.addEdge(new Edge(1,3))

    graph_with_redundance.addEdge(new Edge(2,4))
    graph_with_redundance.addEdge(new Edge(2,4))

    graph_with_redundance.addEdge(new Edge(3,4))
    graph_with_redundance.addEdge(new Edge(3,4))

  }


  def sameElements[T](value1: List[T], value2: List[T]): Boolean =
    value1.forall {
      value2.contains
    } && value1.length == value2.length


  test("Same elements test") {
    val a = List(
      List(1, 2, 3),
      List(1, 2, 4),
      List(5,2,3)
    )

    val b = List(
      List(1, 2, 4),
      List(1, 2, 3),
      List(5,2,3)
    )

    val c = List(List(1))

    assert(sameElements(a,a))
    assert(sameElements(b,b))
    assert(sameElements(c,c))

    assert(sameElements(a,b))
    assert(sameElements(b,a))

    assert(!sameElements(a,c))
    assert(!sameElements(c,a))
    assert(!sameElements(c,b))
    assert(!sameElements(b,c))


  }

  test("Path Verification 0 -> 10 in the graph_multiple_different_paths") {
    //val path: List[List[Long]] = List(List(0l, 4l, 6l, 10l))
    val path: List[Path] = List(
      Path(List(  PathSingleEdge(new Edge(0l,4l)) ,
       PathSingleEdge(new Edge(4l,6l)),
       PathSingleEdge(new Edge(6l ,10l)))
    )
    )
    println(MinimalPathsFinder(graph_multiple_different_paths, 0l, 10l))
    assert(sameElements(MinimalPathsFinder(graph_multiple_different_paths, 0l, 10l), path))
  }

  test("Path Verification 0 -> 7 in the graph_multiple_different_paths") {
    //val path: List[List[Long]] = List(List(0l, 4l, 11l, 7l), List(0l, 4l, 6l, 7l))
    val path: List[Path] = List(
      Path(List(  PathSingleEdge(new Edge(0,4)) ,
       PathSingleEdge(new Edge(4,11)) ,
       PathSingleEdge(new Edge(11,7) ))
      ),
      Path(List( PathSingleEdge(new Edge(0,4)) ,
         PathSingleEdge(new Edge(4,6)) ,
         PathSingleEdge(new Edge(6,7) ) )
      )
    )


    assert(sameElements(MinimalPathsFinder(graph_multiple_different_paths, 0l, 7l), path))
  }

  test("Path Verification 2 -> 8 in the graph_multiple_different_paths") {
    //val path: List[List[Long]] =   List(2l, 11l, 7l, 6l, 9l, 8l))
    val path : List[Path] = List(
      Path(List(  PathSingleEdge(new Edge(2,1)) ,
         PathSingleEdge(new Edge(1,0)) ,
         PathSingleEdge(new Edge(0,3)) ,
         PathSingleEdge(new Edge(3,5)) ,
         PathSingleEdge(new Edge(5,8))  )
      ),
        Path(List(  PathSingleEdge(new Edge(2,11)) ,
         PathSingleEdge(new Edge(11,7)) ,
         PathSingleEdge(new Edge(7,10)) ,
         PathSingleEdge(new Edge(10,9)) ,
         PathSingleEdge(new Edge(9,8) ) )
        ),

      Path(List( PathSingleEdge(new Edge(2,1)) ,
         PathSingleEdge(new Edge(1,4)) ,
         PathSingleEdge(new Edge(4,6)) ,
         PathSingleEdge(new Edge(6,9)) ,
         PathSingleEdge(new Edge(9,8)) )
      ) ,
      Path(List( PathSingleEdge(new Edge(2,1)) ,
         PathSingleEdge(new Edge(1,4)) ,
         PathSingleEdge(new Edge(4,6)) ,
         PathSingleEdge(new Edge(6,5)) ,
         PathSingleEdge(new Edge(5,8)) )
      ),
      Path(List( PathSingleEdge(new Edge(2,11)) ,
         PathSingleEdge(new Edge(11,4)) ,
         PathSingleEdge(new Edge(4,6)) ,
         PathSingleEdge(new Edge(6,5)) ,
         PathSingleEdge(new Edge(5,8)) )
      ),
      Path(List( PathSingleEdge(new Edge(2,11)) ,
         PathSingleEdge(new Edge(11,4)) ,
         PathSingleEdge(new Edge(4,6)) ,
         PathSingleEdge(new Edge(6,9)) ,
         PathSingleEdge(new Edge(9,8)) )
      ),
      Path(List( PathSingleEdge(new Edge(2,11)) ,
         PathSingleEdge(new Edge(11,7)) ,
         PathSingleEdge(new Edge(7,6)) ,
         PathSingleEdge(new Edge(6,5)) ,
         PathSingleEdge(new Edge(5,8)) )
      ),
      Path(List( PathSingleEdge(new Edge(2,11)) ,
         PathSingleEdge(new Edge(11,7)) ,
         PathSingleEdge(new Edge(7,6)) ,
         PathSingleEdge(new Edge(6,9)) ,
         PathSingleEdge(new Edge(9,8)) )
      )
    )

    assert(sameElements(MinimalPathsFinder(graph_multiple_different_paths, 2l, 8l), path))
  }
  test("Path Verification 0 -> 6 in the graph_multiple_cross_paths") {
    //val path: List[List[Long]] =  List(0l, 2l, 3l, 4l, 6l))
    val path :List[Path] = List(
      Path(List(  PathSingleEdge(new Edge(0,1)) ,
         PathSingleEdge(new Edge(1,3)) ,
         PathSingleEdge(new Edge(3,4)) ,
         PathSingleEdge(new Edge(4,6))  )
      ),
      Path(List(  PathSingleEdge(new Edge(0,1)) ,
         PathSingleEdge(new Edge(1,3)) ,
         PathSingleEdge(new Edge(3,5)) ,
         PathSingleEdge(new Edge(5,6))  )
      ),
      Path(List(  PathSingleEdge(new Edge(0,2)) ,
         PathSingleEdge(new Edge(2,3)) ,
         PathSingleEdge(new Edge(3,5)) ,
         PathSingleEdge(new Edge(5,6))  )
      ),
      Path(List(  PathSingleEdge(new Edge(0,2)) ,
         PathSingleEdge(new Edge(2,3)) ,
         PathSingleEdge(new Edge(3,4)) ,
         PathSingleEdge(new Edge(4,6))  )
      )
    )

    assert(sameElements(MinimalPathsFinder(graph_multiple_cross_paths, 0l, 6l), path))

  }

  test("Path Verification 0 -> 0 in the graph_one_node") {
    val path: List[Path] = List(Path(Nil))

    assert(sameElements(MinimalPathsFinder(graph_one_node, 0l, 0l), path))
  }

  test("Path Verification 0 -> 0 in the graph_one_node_with_edge") {
    val path: List[Path] = List(Path(Nil))

    assert(sameElements(MinimalPathsFinder(graph_one_node_with_edge, 0l, 0l), path))
  }

  test("Path Verification 0 -> 1 in the graph_with_nodes_without_edges") {
    val path: List[Path] = List()

    assert(sameElements(MinimalPathsFinder(graph_with_nodes_without_edges, 0l, 1l), path))
  }

  test("Path Verification 0 -> 1 in the graph_with_path_size_1") {
    val path: List[Path] = List(
      Path(List( PathSingleEdge(new Edge(0,1))))
    )

    assert(sameElements(MinimalPathsFinder(graph_with_path_size_1, 0l, 1l), path))
  }

  test("Path verification 0 -> 4 on the graph_with_redundance"){
    val path : List[Path] = List(
      Path(List(  PathMultipleEdge( List( new Edge(0,1) , new Edge(0,1) ) ) ,
         PathMultipleEdge( List( new Edge(1,3) , new Edge(1,3) ) ) ,
         PathMultipleEdge( List( new Edge(3,4) , new Edge(3,4) ) ) )
      ),
      Path(List( PathMultipleEdge( List( new Edge(0,1) , new Edge(0,1) ) ) ,
        PathMultipleEdge(List(new Edge(1,2) , new Edge(1,2) ) ) ,
        PathMultipleEdge(List(new Edge(2,4) , new Edge(2,4) ) )  ) )
    )
    assert(sameElements(MinimalPathsFinder(graph_with_redundance , 0l,4l) , path))
  }
  test("bugzila"){
    var graph: LinkedGraph = NTripleParser.parse("src/main/resources/dbpedia.nt")
    println(MinimalPathsFinder(graph,4730l,4325l))
  }

}
