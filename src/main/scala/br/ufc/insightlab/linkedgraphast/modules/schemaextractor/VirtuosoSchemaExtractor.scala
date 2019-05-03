package br.ufc.insightlab.linkedgraphast.modules.schemaextractor

import java.util.concurrent.TimeUnit

import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import org.apache.jena.query.{QueryExecutionFactory, QueryFactory}
import org.slf4j.LoggerFactory
import scala.collection.JavaConverters._

import scala.util.{Failure, Success, Try}

object VirtuosoSchemaExtractor {

  private val propertiesCountSPARQL =
    """
      |select (count(distinct ?p) as ?c) where {
      |  {
      |    select ?p where {
      |      ?p a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> .
      |    }
      |  }
      |  UNION {
      |    select ?p where {
      |      ?p a <http://www.w3.org/2002/07/owl#DatatypeProperty> .
      |    }
      |  }
      |  UNION {
      |    select ?p where {
      |      ?p a <http://www.w3.org/2002/07/owl#ObjectProperty> .
      |    }
      |  }
      |}
    """.stripMargin

  private val propertiesOffsetSPARQL =
    """
      |select distinct ?p where {
      |  {
      |    select ?p where {
      |      ?p a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> .
      |    }
      |  }
      |  UNION {
      |    select ?p where {
      |      ?p a <http://www.w3.org/2002/07/owl#DatatypeProperty> .
      |    }
      |  }
      |  UNION {
      |    select ?p where {
      |      ?p a <http://www.w3.org/2002/07/owl#ObjectProperty> .
      |    }
      |  }
      |}
      |order by ?p
      |offset
    """.stripMargin


  def generatePropertyDomainSPARQL(property: String): String =
    s"""
      |select distinct ?sc where {
      |  ?s <$property> ?o .
      |  ?s a ?sc .
      |
      |  filter not exists {
      |    ?x rdfs:subClassOf ?sc
      |  }
      |}
    """.stripMargin

  def generatePropertyRangeSPARQL(property: String): String =
    s"""
       |select distinct ?oc where {
       |  ?s <$property> ?o .
       |  ?o a ?oc .
       |
 |  filter not exists {
       |    ?x rdfs:subClassOf ?oc
       |  }
       |}
    """.stripMargin

  def generateLabelsSPARQL(concept: String): String =
    s"""
       |select distinct ?l where {
       |  <$concept> <http://www.w3.org/2000/01/rdf-schema#label> ?l .
       |}
       |
    """.stripMargin

  private val logger = LoggerFactory.getLogger(this.getClass)

  private def insertFromOnSPARQL(sparql: String, uri: String): String = {
    val idx = sparql.toLowerCase.indexOf("where")
    sparql.substring(0, idx) + "from <" + uri + "> " + sparql.substring(idx)
  }

  def runQuery(url: String, baseURI: String)(sparql: String):Iterator[List[String]] = {
    val completeSPARQL = insertFromOnSPARQL(sparql, baseURI)
//    println(completeSPARQL)
    Try({
      val query = QueryFactory.create(completeSPARQL)

      val exec = QueryExecutionFactory.sparqlService(url, query)
      exec.setTimeout(1, TimeUnit.MINUTES)
      val resultSet = exec.execSelect()

      resultSet.asScala.map(result => {
        result.varNames().asScala.map(result.get(_).toString).toList
      })
    })
    match {
      case Success(r) => r
      case Failure(e) =>
        //        e.printStackTrace()
        logger.info(e.toString)
        Nil.iterator
    }
  }

  def runCountQuery(url: String, baseURI: String)(sparql: String):Int = {
    val completeSPARQL = insertFromOnSPARQL(sparql, baseURI)
    //    println(completeSPARQL)
    Try({
      val query = QueryFactory.create(completeSPARQL)

      val exec = QueryExecutionFactory.sparqlService(url, query)
      exec.setTimeout(1, TimeUnit.MINUTES)
      val resultSet = exec.execSelect()

      resultSet.asScala.toList.head.get("?c").toString.takeWhile(_ != '^').toInt

    })
    match {
      case Success(r) => r
      case Failure(e) =>
        //        e.printStackTrace()
        logger.info(e.toString)
        -1
    }
  }

  def apply(endpointURL: String, graphURI: String, batchSize: Int = 1000): LinkedGraph = {
    val graph = new LinkedGraph()
    val runner = runQuery(endpointURL, graphURI)(_)

    println(runCountQuery(endpointURL, graphURI)(propertiesCountSPARQL))
    val numberOfProperties = runCountQuery(endpointURL, graphURI)(propertiesCountSPARQL)

    val properties: List[String] =
      (for(offset <- 0 to numberOfProperties by batchSize) yield runner(propertiesOffsetSPARQL+offset).toList.map(_.head))
        .toList
        .flatten

    println(properties.length)

    graph
  }

  def main(args: Array[String]): Unit = {
    val serviceURL = "http://dbpedia.org/sparql"
    val graphURI = "http://dbpedia.org"

    this(serviceURL, graphURI)
  }

}
