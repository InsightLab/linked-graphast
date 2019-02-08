package br.ufc.insightlab.linkedgraphast.model.link

import br.ufc.insightlab.graphast.model.Edge
import br.ufc.insightlab.linkedgraphast.model.helper.LinkedNodeHelper
import br.ufc.insightlab.linkedgraphast.model.node.{LinkedNode, URI}

/**
  * A class to represent the links on a linked data graph
  * @param _source the link's source
  * @param _uri link's URI
  * @param _target the link's target
  */
abstract class Link(_source: URI, _uri: URI, _target: LinkedNode) extends Edge{
  val uri: URI = _uri
  val source: URI = _source
  val target: LinkedNode = _target

  setFromNodeId(LinkedNodeHelper.getNodeIdByURI(source.uri))
  setToNodeId(LinkedNodeHelper.getNodeIdByURI(target.value))
  setBidirectional(true)

  /**
    * Compares if this link is equals to another
    * @param obj the object that will be compared
    * @return true if the links have the same URI, source and target. False otherwise
    */
  override def equals(obj: scala.Any): Boolean =
    obj.isInstanceOf[Link] &&
      obj.asInstanceOf[Link].source == source &&
      obj.asInstanceOf[Link].target == target

  def copy: Link

  /**
    * Operator for the equals method
    *
    * @see See [[Link.equals()]]
    * @param l the link that will be compared
    * @return true if the links have the same URI, source and target. False otherwise
    */
  def ==(l: Link): Boolean =
    this.equals(l)

  /**
    * Convenient operator for the negation of equals method
    *
    * @see See [[Link.equals()]]
    * @param l the link that will be compared
    * @return true if they aren't equals. False otherwise
    */
  def !=(l: Link): Boolean =
    !this.equals(l)

  override def toString: String = s"$source $uri $target [$getWeight]"
}
