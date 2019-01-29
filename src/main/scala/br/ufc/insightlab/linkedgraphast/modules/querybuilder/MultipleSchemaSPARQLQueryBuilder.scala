package br.ufc.insightlab.linkedgraphast.modules.querybuilder

import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.model.link.{Attribute, Link}
import br.ufc.insightlab.linkedgraphast.model.node.{LinkedNode, Literal}
import org.apache.jena.graph.Triple
import org.apache.jena.query.{Query, QueryFactory}
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.sparql.core.Var
import org.apache.jena.sparql.syntax.{ElementGroup, ElementOptional, ElementTriplesBlock}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object MultipleSchemaSPARQLQueryBuilder {
  private val log = LoggerFactory.getLogger(this.getClass)

  private val model = ModelFactory.createDefaultModel()

  private var URIToVar: Map[String, Var] = Map.empty

  private def getVar(URI: String, block: ElementTriplesBlock = null, prefix: String = ""): Var = {
    val uri = prefix + cleanURI(URI)
    if (URIToVar.contains(uri)) URIToVar(uri)
    else {
      val v = Var.alloc(uri)
      URIToVar += uri -> v
      if (null != block)
        block.addTriple(new Triple(v, model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type").asNode, model.createResource(URI).asNode))

      v
    }
  }

  private def hasVar(URI: String): Boolean =
    URIToVar.contains(cleanURI(URI))

  private def cleanURI(uri: String): String =
    uri.reverse.takeWhile(c => c != '/' && c != '#').reverse.replace(">", "").replace(".", "")


  def apply(tuples: List[(LinkedGraph, Map[Long, List[String]])], schema: LinkedGraph): String = {

    URIToVar = Map.empty
    val body = new ElementGroup()
    val query = QueryFactory.make()
    query.setQuerySelectType()

    for((g, filtersMap) <- tuples)
      if(g.getNumberOfNodes == 1){
        val el = new ElementOptional(SchemaSPARQLQueryBuilder.processSingleElement(g, filtersMap, schema, query))
        if(!el.toString.contains("# Empty BGP"))
          body.addElement(el)
      }
      else{
        val el = new ElementOptional(SchemaSPARQLQueryBuilder.processMultipleElements(g, filtersMap, schema, query))
        if(!el.toString.contains("# Empty BGP"))
          body.addElement(el)
      }

    query.setQueryPattern(body)
    query.toString

  }

}
