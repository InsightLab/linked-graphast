package br.ufc.insightlab.linkedgraphast.modules.schemaextractor

import java.io.{File, PrintWriter}
import java.util.concurrent.TimeUnit

import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.model.link.{Attribute, Relation}
import br.ufc.insightlab.linkedgraphast.model.node.{Literal, URI}
import org.apache.jena.query.{QueryExecutionFactory, QueryFactory}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.concurrent.{Await, Future}

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

object VirtuosoSchemaExtractor {

  private val propertiesTriplePatterns =
    """
      |{
      |    select ?p where {
      |      ?p a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> .
      |      filter(!regex(lcase(str(?p)),"(wiki|[0-9]|\\(|\\)|\\'|\\+|\\,|\\;|\\&|\\!|\\?)"))
      |      filter(regex(str(?p), "http://dbpedia.org/property"))
      |    }
      |  }
      |  UNION {
      |    select ?p where {
      |      ?p a <http://www.w3.org/2002/07/owl#DatatypeProperty> .
      |      filter(!regex(lcase(str(?p)),"(wiki|[0-9]|\\(|\\)|\\'|\\+|\\,|\\;|\\&|\\!|\\?)"))
      |      filter(regex(str(?p), "http://dbpedia.org/property"))
      |    }
      |  }
      |  UNION {
      |    select ?p where {
      |      ?p a <http://www.w3.org/2002/07/owl#ObjectProperty> .
      |      filter(!regex(lcase(str(?p)),"(wiki|[0-9]|\\(|\\)|\\'|\\+|\\,|\\;|\\&|\\!|\\?)"))
      |      filter(regex(str(?p), "http://dbpedia.org/property"))
      |    }
      |  }
    """.stripMargin

  private val propertiesCountSPARQL = s"select (count(distinct ?p) as ?c) where {$propertiesTriplePatterns}"

  private val propertiesOffsetSPARQL =
    s"""
       |select distinct ?p where {
       |  $propertiesTriplePatterns
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
       |    ?x <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?sc
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
       |    ?x <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?oc
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

  private val labelURI = URI("http://www.w3.org/2000/01/rdf-schema#label")
  private val domainURI = URI("http://www.w3.org/2000/01/rdf-schema#domain")
  private val rangeURI = URI("http://www.w3.org/2000/01/rdf-schema#range")
  private val typeURI = URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")

  private val classURI = URI("http://www.w3.org/2002/07/owl#Class")
  private val rdfPropertyURI = URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property")
  private val dataTypeURI = URI("http://www.w3.org/2002/07/owl#DatatypeProperty")
  private val objectPropertyURI = URI("http://www.w3.org/2002/07/owl#ObjectProperty")


  private val logger = LoggerFactory.getLogger(this.getClass)

  private def insertFromOnSPARQL(sparql: String, uri: String): String = {
    val idx = sparql.toLowerCase.indexOf("where")
    sparql.substring(0, idx) + "from <" + uri + "> " + sparql.substring(idx)
  }

  def runQuery(url: String, baseURI: String)(sparql: String): Iterable[List[String]] = {
    val completeSPARQL = insertFromOnSPARQL(sparql, baseURI)
    //    println(completeSPARQL)
    Try({
      val query = QueryFactory.create(completeSPARQL)

      val exec = QueryExecutionFactory.sparqlService(url, query)
      exec.setTimeout(1, TimeUnit.MINUTES)
      val resultSet = exec.execSelect()

      val r = resultSet.asScala.map(result => {
        result.varNames().asScala.map(result.get(_).toString).toList
      }).toList

      exec.close()
      r
    })
    match {
      case Success(r) =>
        r
      case Failure(e) =>
        //        e.printStackTrace()
        logger.warn(s"Error processing query\n$completeSPARQL\n"+e.toString)
        Nil
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
        logger.warn(e.toString)
        -1
    }
  }

  def apply(endpointURL: String, graphURI: String, batchSize: Int = 1000): LinkedGraph = {
    val graph = new LinkedGraph()
    val runner = runQuery(endpointURL, graphURI)(_)

    //    def labels(concept: String): Future[Iterator[List[String]]] = Future { runner(generateLabelsSPARQL(concept)) }
    //    def domains(property: String): Future[Iterator[List[String]]] = Future { runner(generatePropertyDomainSPARQL(property)) }
    //    def ranges(property: String): Future[Iterator[List[String]]] = Future { runner(generatePropertyRangeSPARQL(property)) }
    def labels(concept: String): Iterable[List[String]] =  runner(generateLabelsSPARQL(concept))
    def domains(property: String): Iterable[List[String]] = runner(generatePropertyDomainSPARQL(property))
    def ranges(property: String): Iterable[List[String]] = runner(generatePropertyRangeSPARQL(property))

    val numberOfProperties = runCountQuery(endpointURL, graphURI)(propertiesCountSPARQL)
    logger.info(s"Distinct properties at the database: $numberOfProperties")

    val properties: List[String] =
      (for(offset <- 0 to numberOfProperties by batchSize) yield {
        logger.info(s"Getting properties from $offset to ${offset+batchSize}")
        runner(propertiesOffsetSPARQL+offset).toList.map(_.head)
      })
        .flatten
        .distinct
        .toList

    logger.info(s"${properties.distinct.length} properties retrieved")
//    val writer = new PrintWriter(new File("properties.txt"))
//    properties.foreach(p => writer.write(p+"\n"))
//    writer.close()

    graph.addNodes(rdfPropertyURI, classURI, dataTypeURI, objectPropertyURI)

    properties
//      .take(0)
      .foreach { property => {
        val propertyURI = URI(property)
        this.synchronized({
          graph.addNode(propertyURI)
          graph.addLink(Relation(propertyURI, typeURI, rdfPropertyURI))
        })

        logger.info(s"Getting labels to property $propertyURI")
        val l = labels(property).foreach( i => i.foreach(l => {
          val label = Literal(l)
          this.synchronized(if(!graph.containsNode(label.getId)) graph.addNode(label))
          graph.addLink(Attribute(propertyURI, labelURI, label))
        } ))

        logger.info(s"Getting domains to property $propertyURI")
        domains(property).foreach ( i => i.foreach(d =>{
          val uri = URI(d)
          this.synchronized(
            if(!graph.containsNode(uri.getId)) {
              graph.addNode(uri)
              graph.addLink(Relation(uri, typeURI, classURI))
              labels(uri.uri).foreach(ls => {
                val label = Literal(ls.head)
                if(!graph.containsNode(label.getId)) graph.addNode(label)
                graph.addLink(Attribute(uri, labelURI, label))
              })
            }
          )
          graph.addLink(Relation(propertyURI, domainURI, uri))
        } ))

        logger.info(s"Getting ranges to property $propertyURI")
        val r = ranges(property)

        if(r.isEmpty) graph.addLink(Relation(propertyURI, typeURI, dataTypeURI))
        else graph.addLink(Relation(propertyURI, typeURI, objectPropertyURI))

        r.foreach ( i => i.foreach(r =>{
          val uri = URI(r)
          this.synchronized(
            if(!graph.containsNode(uri.getId)) {
              graph.addNode(uri)
              graph.addLink(Relation(uri, typeURI, classURI))
              labels(uri.uri).foreach(ls => {
                val label = Literal(ls.head)
                if(!graph.containsNode(label.getId)) graph.addNode(label)
                graph.addLink(Attribute(uri, labelURI, label))
              })
            }
          )
          graph.addLink(Relation(propertyURI, rangeURI, uri))
        } ))

      }}

    //    Await.result(Future.sequence(operations), Duration.Inf)
    graph
  }

  def main(args: Array[String]): Unit = {
    val serviceURL = "http://dbpedia.org/sparql"
    val graphURI = "http://dbpedia.org"

    val graph = this(serviceURL, graphURI, 10000)

    println(graph.getNumberOfNodes)
    println(graph.getNumberOfEdges)

    graph.save("extracted-schema.nt")
    //    println(graph.toNTriple)
  }


}