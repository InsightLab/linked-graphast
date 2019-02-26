package br.ufc.insightlab.linkedgraphast.experiments.helper


import java.util.concurrent.TimeUnit

import org.apache.jena.query.{QueryExecutionFactory, QueryFactory}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object VirtuosoHelper {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private def insertFromOnSPARQL(sparql: String, uri: String): String = {
    val idx = sparql.toLowerCase.indexOf("where")
    sparql.substring(0, idx) + "from <" + uri + "> " + sparql.substring(idx)
  }

  def runQuery(url: String, baseURI: String)(sparql: String):Iterator[List[String]] = {
    val completeSPARQL = insertFromOnSPARQL(sparql, baseURI)
//    val completeSPARQL = sparql

    Try({
      val query = QueryFactory.create(completeSPARQL)

      val exec = QueryExecutionFactory.sparqlService(url, query)
      exec.setTimeout(10000, TimeUnit.MINUTES)
      val resultSet = exec.execSelect()

      resultSet.asScala.map(result => {
        result.varNames().asScala.map(result.get(_).toString).toList
      })
    })
    match {
      case Success(r) => r
      case Failure(e) =>
//        e.printStackTrace()
        logger.debug(e.toString)
        Nil.iterator
    }
  }

  def main(args: Array[String]): Unit = {
    val sparql = "select ?s ?p ?o where{?s ?p ?o} LIMIT 10"
    //the source URI from the database
    val baseURI = "http://dbpedia.org"

    println(runQuery("http://dbpedia.org/sparql", baseURI)(sparql).mkString("\n"))
  }

}
