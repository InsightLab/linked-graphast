package br.ufc.insightlab.linkedgraphast.modules.NER.wikifier

import br.ufc.insightlab.linkedgraphast.modules.NER.NERClassifier
import play.api.libs.json.{JsArray, JsObject, Json}
import scalaj.http.{Http, HttpRequest}

import scala.annotation.tailrec
import scala.util.{Success, Try}

object Wikifier extends NERClassifier{

  val wikifierToken: String = sys.env("wikifierToken")

  private case class Annotation(text: String, types: List[String], pageRank: Double)

  @tailrec
  private def retryRequest(req: HttpRequest): String =
    Try(req.asString.body) match {
      case Success(s) => s
      case _ => retryRequest(req)
    }

  def classify(text: String): List[(String, List[String])] = {
    val response: String =
      retryRequest(Http("http://www.wikifier.org/annotate-article")
        .param("userKey",wikifierToken)
        .param("text", text))

//    println(response)

    val annotations: Seq[Annotation] = Json.parse(response)
        .as[JsObject]
        .value("annotations")
        .as[JsArray]
        .value.map(ann =>
          Annotation((ann \ "title").as[String], (ann \ "dbPediaTypes").as[List[String]], (ann \ "pageRank").as[Double])
        )

//    println(annotations.mkString("\n"))

    annotations
      .map(a => (a.text, a.types))
      .filterNot(_._2.isEmpty)
      .map(t => (t._1, List(t._2.head)))
      .toList
  }

  def main(args: Array[String]): Unit = {
    classify("Barack Obama is the president of united states of america")
  }

}
