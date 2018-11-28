package br.ufc.insightlab.linkedgraphast.model.node

/**
  * A class to represent a data instance on a linked data graph.
  *
  * @see See [[LinkedNode]]
  * @author Lucas Peres
  * @version 0.1
  * @param uri the node's URI
  */
case class URI(uri: String) extends LinkedNode(uri)
