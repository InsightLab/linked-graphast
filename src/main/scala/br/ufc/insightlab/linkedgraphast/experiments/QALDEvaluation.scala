package br.ufc.insightlab.linkedgraphast.experiments


import java.io.{File, PrintWriter}

import br.ufc.insightlab.linkedgraphast.experiments.IMDBEvaluation.logger
import br.ufc.insightlab.linkedgraphast.experiments.helper.VirtuosoHelper
import br.ufc.insightlab.linkedgraphast.metrics.{Precision, Recall}
import br.ufc.insightlab.linkedgraphast.modules.NER.figer.Figer
import br.ufc.insightlab.linkedgraphast.modules.NER.wikifier.Wikifier
import br.ufc.insightlab.linkedgraphast.modules.vonqbe.VonQBESparqlBuilder
import br.ufc.insightlab.linkedgraphast.parser.{JenaRdfParser, NTripleParser}
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsArray, Json, Reads}

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

  val string = Source.fromFile("src/evaluation/resources/QALD-9.json").getLines.mkString("\n")

  val data = Json.parse(string)
    .as[JsArray]
    .value
      .map(e =>
        Query((e \ "id").as[Int], (e \ "string").as[String], (e \ "sparql" \ "sparql").as[String])
      )
//    .filter(d => Set[Int](48,72,102,117,124,137,221)(d.id))
//    .filter(d => Set[Int](72)(d.id))

      val graph = NTripleParser.parse("src/main/resources/dbpedia.nt")
//  val graph = JenaRdfParser.parse("src/main/resources/dbpedia.nt")

  def runExperiments(generateSPARQL: Boolean, generateResults: Boolean, computeMetrics: Boolean, thresholds: Seq[Double]): Unit = {
    val metricsWriter = new PrintWriter(new File("metrics.csv"))
    metricsWriter.println("theta,answered questions,von-qbe recall,von-qbe precision,von-qbner recall,von-qbnerprecision")

    for(theta <- thresholds){
      logger.info(s"Running experiments with similarity threshold $theta\n\n")

      val QB = new VonQBESparqlBuilder(graph, theta, Wikifier)

      if(generateResults){
        new File(basePath)
          .listFiles()
          .filter(_.isDirectory)
          .foreach(FileUtils.deleteDirectory)
      }

      val metrics = for(q <- data.filterNot(d => d.sparql.contains("ASK WHERE") || d.sparql.contains("ASK \nWHERE"))) yield {
        if(!computeMetrics) logger.info(s"Processing query ${q.id}: ${q.text}")

        val path = basePath+"/question"+q.id

        FileUtils.forceMkdir(new File(path))

        val query =
          if(generateSPARQL) {
            val query = QB.generateSPARQL(q.text)
            val writer = new PrintWriter(new File(path+"/sparql-generated-simple.txt"))
            writer.write(query)
            writer.close()
            query
          }
          else Source.fromFile(path+"/sparql-generated-simple.txt").mkString

        val queryNER =
          if(generateSPARQL) {
            val query = QB.generateSPARQL(q.text, withNER = true, withMinimalPaths = true)
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
            logger.info(s"query ${q.id}: ${q.text}")
            logger.info(s"Von-QBE recall: $recall | Von-QBE precision: $precision")
            logger.info(s"Von-QBNER recall: $recallNER | Von-QBNER precision: $precisionNER")
          }
          ((recall, precision), (recallNER, precisionNER))
        } else ((0.0,0.0), (0.0,0.0))

      }

      if(computeMetrics && metrics.nonEmpty){
        println("\n------------------------------------------\n")
        logger.info(s"Processed queries: ${metrics.size}")
        val nonZero: Double = metrics.count(x => x._1._1 > 0 || x._1._2 > 0 || x._2._1 > 0 || x._2._2 > 0)
        logger.info(s"$nonZero non-zero results")
        val sums: ((Double, Double), (Double, Double)) = metrics.foldLeft((0.0,0.0), (0.0,0.0))((acc, t) => (((acc._1)._1 + t._1._1 , (acc._1)._2 + t._1._2), ((acc._2)._1 + t._2._1 , (acc._2)._2 + t._2._2)))

        val meanRecall = (sums._1._1/nonZero).nanAsZero
        val meanPrecision = (sums._1._2/nonZero).nanAsZero
        //      logger.info(s"Mean recall without NER: $meanRecall | Mean precision without NER: $meanPrecision")
        logger.info(s"Von-QBE mean recall: $meanRecall | Von-QBE mean precision: $meanPrecision")

        val meanRecallNER = (sums._2._1/nonZero).nanAsZero
        val meanPrecisionNER = (sums._2._2 / nonZero).nanAsZero
        //      logger.info(s"Mean recall with NER: $meanRecallNER | Mean precision with NER: $meanPrecisionNER")
        logger.info(s"Von-QBNER mean recall: $meanRecallNER | Von-QBNER mean precision: $meanPrecisionNER")

        metricsWriter.println(s"$theta,$nonZero,$meanRecall,$meanPrecision,$meanRecallNER,$meanPrecisionNER")

//        println("\n\n###################\n\n")
      }
    }
    metricsWriter.close()
  }

  def main(args: Array[String]): Unit = {
    runExperiments(generateSPARQL = true, generateResults =true, computeMetrics = true,
      List(0.5, 0.6, 0.7, 0.8, 0.9, 1)
//      List(1)
    )
  }

}
