package br.ufc.insightlab.linkedgraphast.model.graph

import java.lang
import java.util.stream.StreamSupport

import scala.collection.JavaConverters._
import br.ufc.insightlab.graphast.model.{Edge, Graph, Node}
import br.ufc.insightlab.graphast.structure.{DefaultGraphStructure, GraphStructure}
import br.ufc.insightlab.linkedgraphast.model.helper.LinkedNodeHelper
import br.ufc.insightlab.linkedgraphast.model.link.{Attribute, Link, Relation}
import br.ufc.insightlab.linkedgraphast.model.node.{LinkedNode, Literal}
import br.ufc.insightlab.linkedgraphast.model.structure.Neo4jStructure


/**
  * A class to represent a Linked Data Graph.
  * A linked data graph consists in a regular data graph (a graph representing relationships for
  * semantic data), where the not only the objects but the relationship links between them is used
  * as components of this structure.
  *
  * @see [[Graph]]
  * @author Lucas Peres
  * @version 0.1
  */
class LinkedGraph(structure: GraphStructure = new DefaultGraphStructure()) extends Graph(structure) {

  /**
    * Gets the Node with specified URI value.
    *
    * @see [[LinkedGraph.getNode()]]
    * @param URI the string of the URI to be searched in the Nodes.
    * @return the node with the given URI, in case it exists in the graph.
    */
  def getNodeByURI(URI: String): Node =
    this.getNode(LinkedNodeHelper.getNodeIdByURI(URI))

  /**
    * Gets the list of links in the graph with the specified link, source and/or target URI.
    * The target can also be a [[Literal]].
    *
    * @param linkURI   the string of the link's URI or null for all links.
    * @param sourceURI the string of the source's URI or null for all sources.
    * @param target    the string of the target's URI/Literal or null for all targets.
    * @return a list of all links with the specified parameters.
    */
  def getLinks(linkURI: String = null, sourceURI: String = null, target: String = null): Stream[Link] =
    for {
      l <- getLinksAsStream
      if null == linkURI || l.uri.uri == linkURI //check link URI
      if null == sourceURI || l.source.uri == sourceURI //check source URI
      if null == target || l.target.value == target
    } yield l


  /**
    * Remove all the Links with the specified link, source and/or target URI.
    * The target can also be a [[Literal]]
    *
    * @see [[LinkedGraph.removeEdge()]]
    * @param linkURI   the link's URI string or null for all links.
    * @param sourceURI the source's URI string or null for all sources.
    * @param target    the target's URI/Literal string or null for all targets.
    * @return a list of all the removed links.
    */
  def removeLinks(linkURI: String = null, sourceURI: String = null, target: String = null): List[Link] =
    if (null == linkURI && null == sourceURI && null == target)
      List()
    else
      (for {
        l <- getLinks(linkURI, sourceURI, target)

      } yield {
        removeEdge(l.getId)
        l
      }).toList

  /**
    * Remove all the Links with the specified link, source and/or target URI.
    * The target can also be a [[Literal]]
    *
    * @see [[LinkedNodeHelper.getNodeIdByURI()]] and [[LinkedGraph.removeNode()]]
    * @param URI the URI string of the Node to be removed.
    * @return the removed Node.
    */
  def removeNode(URI: String): Unit =
    super.removeNode(LinkedNodeHelper.getNodeIdByURI(URI, generateId = false))

  /**
    * Adds a new Link component directly to the graph.
    *
    * @see [[Edge]], [[Link]] and [[LinkedNodeHelper.getNodeIdByURI()]]
    * @param l the Link to be added to the graph.
    */
  def addLink(l: Link): Unit = l match {

    case a: Attribute =>

      if (!containsNode(a.value)) {
        addNode(a.target)
      }
      super.addEdge(l)

    case _ =>
      super.addEdge(l)

  }

  def getInstancesDefinitions(typeURI: String = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"): Stream[Relation] = structure match {
    case s: Neo4jStructure => s.getInstancesDefinitions(typeURI).asScala.toStream

    case _ => getLinksAsStream
      .filter(_.uri.uri == typeURI)
      .map(_.asInstanceOf[Relation])
  }

  /**
    * Gets all the classes existent in the graphs components.
    *
    * @return a Set of strings of all the classes in the graph.
    */
  def getClasses: Set[String] =
    getLinksAsStream
      .filter(l => l.uri.uri.endsWith("#type"))
      .map(_.target.value)
      .toSet

  /**
    * Gets all the instances existent in the graphs components.
    *
    * @return a List of strings pairs of all the instances in the graph.
    */
  def getInstances: List[(String, String)] =
    getLinksAsStream
      .filter(l => l.uri.uri.endsWith("#type"))
      .map(l => (l.source.uri, l.target.value))
      .toList

  /**
    * Checks if a Node with a given URI already exists in the graph.
    *
    * @see [[LinkedGraph.containsNode()]]
    * @return true if there exists a Node with the given URI. False otherwise.
    */
  def containsNode(URI: String): Boolean =
    super.containsNode(LinkedNodeHelper.getNodeIdByURI(URI))

  /**
    * Gets all the links of the Graph as a sequential stream of data.
    *
    * @see [[StreamSupport]]
    * @return a sequential Stream of all the Edges in the graph.
    */
  def getLinksAsStream: Stream[Link] =
    StreamSupport.stream(super.getEdges.spliterator(), false).iterator().asScala.toStream.map {
      case l: Link => l
      case x: Edge =>
        println(x)
        throw new Error("Unsuported edge found")
    }

  /**
    * Gets all the outgoing edges leaving a specified Node of the Graph as a sequential stream of data.
    *
    * @see [[StreamSupport]]
    * @param id the identifier of the Node from where the Edges are leaving
    * @return a sequential Stream of all the outgoing Edges in the graph leaving the specified Node.
    */
  def getOutEdgesAsStream(id: Long): Stream[Edge] =
    StreamSupport.stream(super.getOutEdges(id).spliterator(), false).iterator().asScala.toStream

  /**
    * Gets all the Literals contained in the Graph as a sequential stream of data.
    *
    * @see [[StreamSupport]]
    * @return a sequential Stream of all the Literals in the graph.
    */
  def getLiterals: Stream[Literal] = structure match {
    case s:Neo4jStructure => s.getLiterals.asScala.toStream

    case _ => StreamSupport.stream(getNodes.spliterator(), false)
      .iterator().asScala
      .toStream
      .filter(_.isInstanceOf[Literal])
      .map(_.asInstanceOf[Literal])
  }

  def getWords: Stream[(org.neo4j.graphdb.Node, String)] = structure match {
    case s:Neo4jStructure => s.getWords.asScala.toStream

    case _ => throw new Error("Method getWords works only with graph using Neo4jStructure")
  }

  def getNeo4jStructure: Neo4jStructure = structure match {
    case s:Neo4jStructure => s

    case _ => throw new Error("Structure isn't a Neo4jStructure")
  }

  /**
    * Adds a specified Node to the graph.
    * This override garanties that the Node inserted is a Linked Component
    *
    * @see [[LinkedGraph.addNode()]]
    * @param n the Node to be inserted in the graph.
    */
  override def addNode(n: Node): Unit =
    if (!n.isInstanceOf[LinkedNode])
      throw new Error("Only LinkedNodes insertion is allowed")
    else {
      super.addNode(n)
    }

  /**
    * This override garanties that the Node inserted is a Linked Component
    *
    * @param id the id of the Node to be inserted in the graph.
    */
  override def addNode(id: Long): Unit =
    throw new Error("Only Nodes with LinkedNodeComponent insertion is allowed")

  override def getNodes: lang.Iterable[Node] = super.getNodes

  def hasLink(link: Link): Boolean = {
    val e1 = super.getEdge(link.source.getId, link.target.getId)
    val e2 = super.getEdge(link.target.getId, link.source.getId)

    null != e1 || null != e2
  }

}
