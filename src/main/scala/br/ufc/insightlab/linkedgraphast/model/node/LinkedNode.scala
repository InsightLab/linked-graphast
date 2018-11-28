package br.ufc.insightlab.linkedgraphast.model.node

import org.insightlab.graphast.model.Node
import br.ufc.insightlab.linkedgraphast.model.helper.LinkedNodeHelper

/**
  * A class to represent a node on a linked data graph.
  *
  * The node can be a [[URI]] or an [[Literal]]
  *
  * @author Lucas Peres
  * @version 0.1
  * @param _value the components's value
  */
abstract class LinkedNode(_value: String) extends Node{

  setId(LinkedNodeHelper.getNodeIdByURI(value))

  /**
    * Gets the component's value
    * @return the component's value
    */
  def value: String = _value

  /**
    * Operator for the equals method
    *
    * @see See [[LinkedNode.equals()]]
    * @param node the node that will be compared
    * @return true if the nodes have the same value. False otherwise
    */
  def ==(node: LinkedNode): Boolean =
    equals(node)

  /**
    * Convenient operator for the negation of equals method
    *
    * @see See [[LinkedNode.equals()]]
    * @param node the node that will be compared
    * @return true if the nodes have different value. False otherwise
    */
  def !=(node: LinkedNode): Boolean =
    !equals(node)

  /**
    * Compares if this node is equals to another
    * @param obj the object that will be compared
    * @return true if the nodes have the same value. False otherwise
    */
  override def equals(obj: Any): Boolean =
    obj.isInstanceOf[LinkedNode] &&
      this.value == obj.asInstanceOf[LinkedNode].value

  override def toString: String = s"${this._value}{${this.getId}}"
}
