package br.ufc.insightlab.linkedgraphast.model.link

import br.ufc.insightlab.linkedgraphast.model.node.{URI, Literal}

/**
  * A class to represent attribute links on a linked data graph.
  * The attribute is a link from a data instance to a literal
  *
  * @see See [[br.ufc.insightlab.linkedgraphast.model.node.URI]] and [[br.ufc.insightlab.linkedgraphast.model.node.Literal]]
  * @param _source the instance that has the attribute
  * @param _uri attribute's URI
  * @param _value the attribute's value for the instance
  */
case class Attribute(_source: URI, _uri: URI, _value: Literal) extends Link(_source, _uri, _value) {

  /**
    * @constructor An alternative constructor where the literal can be a string instead a Literal object
    * @param _source the link's source
    * @param _uri link's URI
    * @param _value the link's target
    * @return
    */
  def this(_source: URI, _uri: URI, _value: String) =
    this(_source, _uri, Literal(_value))

  /**
    * Gets the attribute's value
    * @return the attribute's value
    */
  def value: String = _value.value

  def copy: Link = Attribute(source,uri,_value)
}
