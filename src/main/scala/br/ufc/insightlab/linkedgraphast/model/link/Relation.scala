package br.ufc.insightlab.linkedgraphast.model.link

import br.ufc.insightlab.linkedgraphast.model.node.URI

/**
  * A class to represent a relation between two classes or instances on a linked data graph.
  *
  * @see See [[br.ufc.insightlab.linkedgraphast.model.node.URI]] and  [[br.ufc.insightlab.linkedgraphast.model.node.URI]]
  * @author Lucas Peres
  * @version 0.1
  * @param _source the relation's source
  * @param _uri the relation's URI
  * @param _target the relation's target
  */
case class Relation(_source: URI, _uri: URI, _target: URI) extends Link(_source, _uri, _target) {
  def copy: Link = Relation(source,uri,_target)
}
