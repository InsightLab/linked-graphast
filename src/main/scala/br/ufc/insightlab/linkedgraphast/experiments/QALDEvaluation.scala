package br.ufc.insightlab.linkedgraphast.experiments


import java.io.{File, PrintWriter}

import br.ufc.insightlab.linkedgraphast.experiments.IMDBEvaluation.logger
import br.ufc.insightlab.linkedgraphast.experiments.helper.VirtuosoHelper
import br.ufc.insightlab.linkedgraphast.metrics.{Precision, Recall}
import br.ufc.insightlab.linkedgraphast.modules.figer.Figer
import br.ufc.insightlab.linkedgraphast.modules.vonqbe.VonQBESparqlBuilder
import br.ufc.insightlab.linkedgraphast.parser.NTripleParser
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsArray, Json}

import scala.io.Source

object QALDEvaluation{

  val exec:String => Iterator[List[String]] = VirtuosoHelper.runQuery("http://dbpedia.org/sparql","http://dbpedia.org")

  private val logger = LoggerFactory.getLogger(this.getClass)

  def serializeResults(path: String, results: Iterator[List[String]]): Unit = {

    val writer = new PrintWriter(new File(path))
    writer.println(" ")
    results.foreach(r => writer.println(r.mkString("\t")))

    writer.close()
  }

  def processQueries(benchmark: String, generated: String, generatedNER: String, folderPath: String): Unit = {
    serializeResults(s"$folderPath/benchmark-results.tsv",exec(benchmark))
    serializeResults(s"$folderPath/generated-results-simple.tsv",exec(generated))
    serializeResults(s"$folderPath/generated-results-ner.tsv",exec(generatedNER))
  }

  case class Query(id: Int, text: String, sparql: String)

  val basePath = "src/evaluation/resources/QALD"

  val string = Source.fromFile("src/evaluation/resources/QALD-6.json").getLines.mkString("\n")

  val data = Json.parse(string)
    .as[JsArray]
    .value
      .map(e =>
        Query((e \ "id").as[Int], (e \ "string").as[String], (e \ "sparql" \ "sparql").as[String])
      )


  def runExperiments(generateSPARQL: Boolean, generateResults: Boolean, computeMetrics: Boolean): Unit = {
    val graph = NTripleParser.parse("src/main/resources/dbpedia.nt")
    val QB = new VonQBESparqlBuilder(graph)
    if(generateSPARQL){
      Figer.init("src/main/resources/figer.conf")
    }

    if(generateResults){
      new File(basePath)
        .listFiles()
        .filter(_.isDirectory)
        .foreach(FileUtils.deleteDirectory)
    }

    val metrics = for(q <- data.filterNot(d => d.sparql.contains("ASK WHERE") || d.sparql.contains("ASK \nWHERE"))) yield {
      logger.info(s"Processing query ${q.id}: ${q.text}")
      val path = basePath+"/question"+q.id

      FileUtils.forceMkdir(new File(path))

      val query =
        if(generateSPARQL) {
          val query = QB.generateSPARQL(q.text, false)
          val writer = new PrintWriter(new File(path+"/sparql-generated-simple.txt"))
          writer.write(query)
          writer.close()
          query
        }
        else Source.fromFile(path+"/sparql-generated-simple.txt").mkString

      val queryNER =
        if(generateSPARQL) {
          val query = QB.generateSPARQL(q.text, true)
          val writer = new PrintWriter(new File(path+"/sparql-generated-ner.txt"))
          writer.write(query)
          writer.close()
          query
        }
        else Source.fromFile(path+"/sparql-generated-ner.txt").mkString

      if(generateResults) processQueries(q.sparql, query, queryNER, path)

      if(computeMetrics){
        val recall = Recall(s"$path/benchmark-results.tsv",
          s"$path/generated-results-simple.tsv")

        val precision = Precision(s"$path/benchmark-results.tsv",
          s"$path/generated-results-simple.tsv")

        val recallNER = Recall(s"$path/benchmark-results.tsv",
          s"$path/generated-results-ner.tsv")

        val precisionNER = Precision(s"$path/benchmark-results.tsv",
          s"$path/generated-results-ner.tsv")

        if(recall > 0 || precision > 0 || recallNER > 0 || precisionNER > 0){
          logger.debug(s"query ${q.id}: ${q.text}")
          logger.debug(s"Recall value without NER: $recall | Precision value without NER: $precision")
          logger.debug(s"Recall value with NER: $recall | Precision value with NER: $precision")
        }
        ((recall, precision), (recallNER, precisionNER))
      } else ((0.0,0.0), (0.0,0.0))

    }

    if(computeMetrics){
      println("\n------------------------------------------\n")
      logger.info(s"Processed queries: ${metrics.size}")
      val nonZero: Double = metrics.count(x => x._1._1 > 0 || x._1._2 > 0 || x._2._1 > 0 || x._2._2 > 0)
      println(s"$nonZero non-zero results!")
      val sums: ((Double, Double), (Double, Double)) = metrics.foldLeft((0.0,0.0), (0.0,0.0))((acc, t) => (((acc._1)._1 + t._1._1 , (acc._1)._2 + t._1._2), ((acc._2)._1 + t._2._1 , (acc._2)._2 + t._2._2)))

      val meanRecall = (sums._1)._1 / metrics.size
      val meanPrecision = (sums._1)._2 / metrics.size
      logger.info(s"Mean recall without NER: $meanRecall | Mean precision without NER: $meanPrecision")
      logger.info(s"Mean non-zero recall without NER: ${(sums._1)._1/nonZero} | Mean non-zero precision without NER: ${(sums._1)._2/nonZero}")

      val meanRecallNER = (sums._2)._1 / metrics.size
      val meanPrecisionNER = (sums._2)._2 / metrics.size
      logger.info(s"Mean recall with NER: $meanRecallNER | Mean precision with NER: $meanPrecisionNER")
      logger.info(s"Mean non-zero recall with NER: ${(sums._2)._1/nonZero} | Mean non-zero precision with NER: ${(sums._2)._2/nonZero}")
    }

  }

  def main(args: Array[String]): Unit = {
    val metrics = false
    runExperiments(!metrics, !metrics, metrics)
  }

}
