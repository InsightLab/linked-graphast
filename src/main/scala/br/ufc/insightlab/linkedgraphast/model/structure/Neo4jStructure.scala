package br.ufc.insightlab.linkedgraphast.model.structure

import java.io.File
import java.util

import org.apache.commons.io.FileUtils
import br.ufc.insightlab.graphast.exceptions.DuplicatedNodeException
import br.ufc.insightlab.graphast.model.{Edge, Node}
import br.ufc.insightlab.graphast.model.components.GraphComponent
import br.ufc.insightlab.graphast.structure.GraphStructure
import br.ufc.insightlab.linkedgraphast.model.helper.LinkedNodeHelper
import br.ufc.insightlab.linkedgraphast.model.link.{Attribute, Link, Relation}
import br.ufc.insightlab.linkedgraphast.model.node.{Literal, URI}
import org.neo4j.graphdb._
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success, Try}


class Neo4jStructure(path: String = "linkedgraph.db", reset: Boolean = true) extends GraphStructure {

  object NodeLabel extends Label {
    override def name(): String = "Node"
  }

  object URILabel extends Label {
    override def name(): String = "URI"
  }

  object LiteralLabel extends Label {
    override def name(): String = "Literal"
  }

  object WordLabel extends Label {
    override def name(): String = "Word"
  }

  private val log: Logger = LoggerFactory.getLogger(this.getClass)
  private var ids: scala.collection.mutable.Map[Long, Long] = scala.collection.mutable.Map()

  var nodes = 0
  var edges = 0

  log.debug(s"Creating neo4j graph at path $path")
  private var graphDB: GraphDatabaseService = _

  if(reset) {
    log.info(s"Reseting db at $path")
    FileUtils.deleteQuietly(new File(path))
    graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(new File(path))
  }
  else{
    graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(new File(path))
    doTx({
      log.info(s"Loading db at $path")
      nodes = graphDB.execute(
        s"MATCH (n: Node) RETURN count(*)"
      ).next.get("count(*)").toString.toInt
      log.info(s"$nodes nodes loaded")
      edges = graphDB.execute(
        s"MATCH (n)-[]->() RETURN count(*)"
      ).next.get("count(*)").toString.toInt
      log.info(s"$edges edges on the graph")
    })
  }

  log.info("Graph created")

  object RelTypes extends Enumeration {
    type RelTypes = Value
    val NEIGH: Value = Value

    implicit def conv(rt: RelTypes): RelationshipType = new RelationshipType() {
      def name: String = rt.toString
    }
  }

  def shutdown(): Unit = graphDB.shutdown()

  def doTx(f: => Unit): Unit = {
    val tx = graphDB.beginTx
    try {
      //      log.debug("Starting transaction")
      f
      //      log.debug("Success")
      tx.success()
    }
    catch {
      case e: Exception =>
        log.debug(s"Exception thrown: $e")
        throw e
    }
    finally {
      //      log.debug("Terminating transaction")
      tx.close()
    }
  }

  def mapNode(n: org.neo4j.graphdb.Node): Node = {
    val id = n.getProperty("id").toString.toLong

    if (n.hasLabel(LiteralLabel)) {
      if(!ids.contains(id)) {
        ids(id) = n.getId
        LinkedNodeHelper.setURIId(n.getProperty("value").toString, id)
      }
      Literal(n.getProperty("value").toString)
    }
    else if (n.hasLabel(URILabel)){
      if(!ids.contains(id)) {
        ids(id) = n.getId
        LinkedNodeHelper.setURIId(n.getProperty("uri").toString, id)
      }
      URI(n.getProperty("uri").toString)
    }
    else
      new Node(n.getProperty("id").toString.toLong)
  }

  def mapEdge(relationship: org.neo4j.graphdb.Relationship): Edge = {
    val fromNode = mapNode(relationship.getStartNode)
    val toNode = mapNode(relationship.getEndNode)

    val e =
      if (relationship.hasProperty("uri")) {
        val uri = URI(relationship.getProperty("uri").toString)

        toNode match {
          case u: URI =>
            Relation(fromNode.asInstanceOf[URI], uri, u)

          case l: Literal =>
            Attribute(fromNode.asInstanceOf[URI], uri, l)
        }

      }
      else
        new Edge(fromNode.getId, toNode.getId)

    e.setId(relationship.getId)
    e.setBidirectional(true)

    if(relationship.hasProperty("weight"))
      e.setWeight(relationship.getProperty("weight").toString.toDouble)
    else e.setWeight(1.0)

    e
  }

  def getLiterals: util.Iterator[Literal] = {
    new util.Iterator[Literal] {
      private val tx = graphDB.beginTx
      private val it = graphDB.findNodes(LiteralLabel)

      override def hasNext: Boolean = it.hasNext

      override def next(): Literal = {
        val n = mapNode(it.next).asInstanceOf[Literal]
        if (!hasNext) {
          tx.success()
          tx.close()
        }
        n
      }
    }
  }

  def getWords: util.Iterator[(org.neo4j.graphdb.Node, String)] = {
    new util.Iterator[(org.neo4j.graphdb.Node, String)] {
      private val tx = graphDB.beginTx
      private val it = graphDB.findNodes(WordLabel)

      override def hasNext: Boolean = it.hasNext

      override def next(): (org.neo4j.graphdb.Node, String) = {
        val n = it.next
        val result = (n, n.getProperty("word").toString)

        if (!hasNext) {
          tx.success()
          tx.close()
        }

        result
      }
    }
  }

  def getInstancesDefinitions(typeURI: String = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"): util.Iterator[Relation] = {
    new util.Iterator[Relation] {
      private val tx = graphDB.beginTx
      private val it = graphDB.execute(
        s"MATCH (s)-[r:`$typeURI`]->(o) RETURN s.uri, id(r), o.uri"
      )

      override def hasNext: Boolean = it.hasNext

      override def next(): Relation = {
        val r = it.next()
        val e = Relation(URI(r.get("s.uri").toString), URI(typeURI), URI(r.get("o.uri").toString))
        e.setId(r.get("id(r)").toString.toInt)
        if (!hasNext) {
          tx.success()
          tx.close()
        }
        e
      }
    }
  }

  override def addNode(node: Node): Unit = {
    doTx({
      val n = graphDB.createNode
      n.addLabel(NodeLabel)
      if (ids.get(node.getId).isEmpty) {
        ids(node.getId) = n.getId
        n.setProperty("id", node.getId)

        node match {
          case u: URI =>
            n.setProperty("uri", u.uri)
            n.addLabel(URILabel)
          case l: Literal =>
            n.setProperty("value", l.value)
            n.addLabel(LiteralLabel)
          case _ =>
        }
      } else throw new DuplicatedNodeException(node.getId)
    })

  }

  override def removeNode(id: Long): Node =
    super.removeNode(id)

  override def removeEdge(e: Edge): Edge = removeEdge(e.getId)

  override def removeEdge(id: Long): Edge = {
    var edge: Edge = null

    doTx({
      val relationship = graphDB.getRelationshipById(id)
      edge = mapEdge(relationship)
      relationship.delete()
    })

    edge
  }

  override def getNode(id: Long): Node = {
    var node: Node = null

    doTx({
      if(ids.contains(id))
        node = mapNode(graphDB.getNodeById(ids(id)))
      else {
        val n =graphDB.findNodes(NodeLabel,"id",id).next()
        node = mapNode(n)
      }
    })

    node

  }

  override def addEdge(edge: Edge): Unit = {
    doTx({
      val fromNode = Try(graphDB.getNodeById(ids(edge.getFromNodeId))) match {
        case Success(node) => node
          case _ =>
          val n = graphDB.findNodes(NodeLabel,"id",edge.getFromNodeId).next()
          ids(edge.getFromNodeId) = n.getId
          n
      }

      val toNode = Try(graphDB.getNodeById(ids(edge.getToNodeId))) match {
        case Success(node) => node
        case _ =>
          val n = graphDB.findNodes(NodeLabel,"id",edge.getToNodeId).next()
          ids(edge.getToNodeId) = n.getId
          n
      }

      val relationship = fromNode.createRelationshipTo(toNode, RelTypes.NEIGH)
      relationship.setProperty("id", edge.getId)
      relationship.setProperty("weight", edge.getWeight)

      edge match {
        case l: Link => relationship.setProperty("uri", l.uri.uri)
      }

      edge.setId(relationship.getId)
    })
  }

  override def getEdge(id: Long): Edge = {
    var e: Edge = null
    doTx({
      val relationship = graphDB.getRelationshipById(id)
      e = mapEdge(relationship)
    })

    e
  }

  override def containsNode(id: Long): Boolean = {
    var contains = false
    doTx({
      try {
        contains = true
      }
      catch {
        case _: NotFoundException =>

        case _: NoSuchElementException =>

        case e: Exception =>
          throw e

      }
    })
    contains
  }


  override def allNodesIterator(): util.Iterator[Node] = {
    new util.Iterator[Node] {
      private val tx = graphDB.beginTx
      private val it = graphDB.findNodes(NodeLabel)

      override def hasNext: Boolean = it.hasNext

      override def next(): Node = {
        val n = mapNode(it.next)

        if (!hasNext) {
          tx.success()
          tx.close()
        }

        n

      }
    }
  }

  override def allEdgesIterator(): util.Iterator[Edge] = {
    new util.Iterator[Edge] {
      log.debug("Creating iterator to all the edges")
      private val tx = graphDB.beginTx
      private val it = graphDB.getAllRelationships.iterator()

      private var nextEdge = tryGetNextEdge(it)

      override def hasNext: Boolean = nextEdge.isDefined

      override def next(): Edge = {
        val e = nextEdge.get

        nextEdge = tryGetNextEdge(it)
        if (nextEdge.isEmpty) {
          tx.success()
          tx.close()
          None
        }
        e
      }
    }
  }

  override def getNumberOfNodes: Long = nodes

  override def getNumberOfEdges: Long = edges

  private def tryGetNextEdge(it: util.Iterator[Relationship]): Option[Edge] = {
    var edge: Option[Edge] = None

    while(edge.isEmpty && it.hasNext) {
      edge = Try(mapEdge(it.next())) match {
        case Success(e) => Some(e)
        case Failure(_) =>
          None
      }
    }

    edge
  }

  override def getAllOutEdgesIterator(id: Long): util.Iterator[Edge] =
    new util.Iterator[Edge] {
      log.debug(s"Creating iterator to out edges from node $id")
      private val tx = graphDB.beginTx

      private val node = Try(graphDB.getNodeById(ids(id))) match {
        case Success(n) => n
        case _ =>
          val n = graphDB.findNodes(NodeLabel,"id",id).next()
          ids(id) = n.getId
          n
      }

      private val it = node.getRelationships.iterator()

      private var nextEdge = tryGetNextEdge(it)

      override def hasNext: Boolean = nextEdge.isDefined

      override def next(): Edge = {
        val e = nextEdge.get

        nextEdge = tryGetNextEdge(it)
          if(nextEdge.isEmpty) {
            tx.success()
            tx.close()
            None
          }
//        log.debug(s"Returning edge $e")
        e
      }
    }

  override def getAllInEdgesIterator(id: Long): util.Iterator[Edge] = getAllOutEdgesIterator(id)

  override def getAllComponentClasses: util.Set[Class[_ <: GraphComponent]] = throw new Error("Method not Implemented to Neo4J graph structure")

  override def getComponent(componentClass: Class[_ <: GraphComponent]): GraphComponent = throw new Error("Method not Implemented to Neo4J graph structure")

  override def addComponent(key: Class[_ <: GraphComponent], component: GraphComponent): Unit = throw new Error("Method not Implemented to Neo4J graph structure")

  override def getAllComponentsIterator: util.Iterator[GraphComponent] = throw new Error("Method not Implemented to Neo4J graph structure")

  override def updateAdjacency(e: Edge): Unit =
    doTx({
      Try(graphDB.getRelationshipById(e.getId)) match {
        case Success(rel) =>
          rel.setProperty("weight",e.getWeight)
        case _ => throw new Error(s"Edge ${e.getId} not found")
      }
    })


  override def containsEdge(id: Long): Boolean = {
    var contains = false

    doTx({
      Try(graphDB.getRelationshipById(id)) match {
        case Success(_) => contains = true
        case _ =>

      }
    })

    contains
  }

  override def nodeIndex(nodeId: Long): Long = {
    throw new Error("Method not implemented yet!")
  }

  override def edgeIndex(edgeId: Long): Long = {
    throw new Error("Method not implemented yet!")
  }

  override def removeNode(n: Node): Node = {
    throw new Error("Method not implemented yet!")
  }

  override def isRemoved(n: Node): Boolean = {
    throw new Error("Method not implemented yet!")
  }

  override def isRemoved(e: Edge): Boolean = {
    throw new Error("Method not implemented yet!")
  }
}
