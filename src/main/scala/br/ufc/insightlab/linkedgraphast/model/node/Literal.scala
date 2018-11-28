package br.ufc.insightlab.linkedgraphast.model.node

/**
  * A class to represent a Literal on a linked data graph.
  * A Literal is a value without URI
  *
  * @see See [[LinkedNode]]
  * @author Lucas Peres
  * @version 0.1
  * @param _value the literal's value
  */
case class Literal(_value: String) extends LinkedNode(_value) {

}
