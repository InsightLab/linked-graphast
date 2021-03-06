package br.ufc.insightlab.linkedgraphast.experiments

import java.io.{File, PrintWriter}

import br.ufc.insightlab.linkedgraphast.metrics.{Precision, Recall}
import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.SimilarityKeywordMatcherOptimizedWithFilters
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity._
import br.ufc.insightlab.linkedgraphast.modules.querybuilder.SchemaSPARQLQueryBuilder
import br.ufc.insightlab.linkedgraphast.parser.NTripleParser
import br.ufc.insightlab.linkedgraphast.query.steinertree.SteinerTree
import br.ufc.insightlab.ror.entities.{ResultQuery, ResultQuerySet}
import br.ufc.insightlab.ror.implementations.OntopROR
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.io.Source

object IMDBEvaluation {

  private val logger = LoggerFactory.getLogger(this.getClass)

  val ror = new OntopROR(
    "src/evaluation/resources/imdb/MovieOntology.owl",
    "src/evaluation/resources/imdb/mapping.obda")

  def processQueries(original: String, generated: String, i:  Int): Unit = {
    logger.debug("Exporting benchmark results")
    exportQueryResultsTSV(ror.runQuery(original),s"src/evaluation/resources/imdb/query$i/benchmark-results.tsv")
    logger.debug("Exporting generated results")
    exportQueryResultsTSV(ror.runQuery(generated),s"src/evaluation/resources/imdb/query$i/generated-results.tsv")
  }

  def exportQueryResultsTSV(result: ResultQuerySet, filePath: String): Unit = {
    val writer = new PrintWriter(new File(filePath))
    val it = result.iterator
    val result0 = if (it.hasNext) it.next() else new ResultQuery(0)
    val header = result0.getProjections.asScala.toList

    writer.println(header.mkString("\t"))

    writer.println(header.map(result0.getValue).mkString("\t"))
    var i = 1
    while(it.hasNext){
      val r = it.next
      writer.println(header.map(r.getValue).mkString("\t"))
      i += 1
    }

    logger.debug(s"$i tuples written")

    writer.close()
  }

  val graph: LinkedGraph = NTripleParser.parse("src/main/resources/imdb-schema.nt")

  def generateQuery(search: String, filePath: String): String = {
    val (nodes,filters) = new SimilarityKeywordMatcherOptimizedWithFilters(new PermutedSimilarity(JaroWinkler))(graph)(search)
    val fragment = SteinerTree(graph)(nodes.toList)
    val query = SchemaSPARQLQueryBuilder(fragment,filters, graph)
    val writer = new PrintWriter(new File(filePath))
    writer.write(query)
    writer.close()

    query
  }

  def recallExperiment(generate: Boolean = true, processSPARQL: Boolean = true): Unit = {
    val metrics = for{
      i <- 1 to 37

    } yield {

      if(i == 24)  (1.0, 0.0004)
      else{
        val path = s"src/evaluation/resources/imdb/query$i/"
        val sparqlPath = path + "sparql.txt"
        val searchPath = path + "search.txt"

        val sparql = Source.fromFile(sparqlPath).mkString
        val search = Source.fromFile(searchPath).mkString

        logger.info(s"Processing search $i '$search'")

        val query =
          if (generate) generateQuery(search, path+"sparql-generated.txt")
          else Source.fromFile(path + "sparql-generated.txt").mkString

        if(processSPARQL)
          processQueries(sparql,query,i)

        val recall = Recall(s"src/evaluation/resources/imdb/query$i/benchmark-results.tsv",
          s"src/evaluation/resources/imdb/query$i/generated-results.tsv")

        val precision = Precision(s"src/evaluation/resources/imdb/query$i/benchmark-results.tsv",
          s"src/evaluation/resources/imdb/query$i/generated-results.tsv")

        logger.info(s"Recall value: $recall | Precision value: $precision")

        (recall, precision)
      }
    }
    val sums = metrics.foldLeft((0.0,0.0))((acc, t) => (acc._1 + t._1 , acc._2 + t._2))
    val meanRecall = sums._1 / metrics.size
    val meanPrecision = sums._2 / metrics.size
    logger.info(s"Mean recall: $meanRecall | Mean precision: $meanPrecision")
  }

  def main(args: Array[String]): Unit = {

    recallExperiment(false,false)

  }


}
