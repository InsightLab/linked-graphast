package br.ufc.insightlab.linkedgraphast.experiments.helper

import org.apache.jena.query.{QueryExecutionFactory, QueryFactory}
import scala.collection.JavaConverters._

object VirtuosoHelper {

  private def insertFromOnSPARQL(sparql: String, uri: String): String = {
    val idx = sparql.toLowerCase.indexOf("where")
    sparql.substring(0, idx) + "from <" + uri + "> " + sparql.substring(idx)
  }

  def runQuery(url: String, baseURI: String)(sparql: String):Iterator[List[String]] = {
    val completeSPARQL = insertFromOnSPARQL(sparql, baseURI)

    val query = QueryFactory.create(completeSPARQL)

    val exec = QueryExecutionFactory.sparqlService(url, query)

    val resultSet = exec.execSelect()

    resultSet.asScala.map(result => {
      result.varNames().asScala.map(result.get(_).toString).toList
    })
  }

  def main(args: Array[String]): Unit = {
    val sparql = "select ?s ?p ?o where{?s ?p ?o} LIMIT 10"
    //the source URI from the database
    val baseURI = "http://dbpedia.org"

    println(runQuery("http://dbpedia.org/sparql", baseURI)(sparql).mkString("\n"))
  }

}
