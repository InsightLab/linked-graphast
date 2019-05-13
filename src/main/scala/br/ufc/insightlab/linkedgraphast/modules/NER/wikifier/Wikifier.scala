package br.ufc.insightlab.linkedgraphast.modules.NER.wikifier

import br.ufc.insightlab.linkedgraphast.modules.NER.NERClassifier
import play.api.libs.json.{JsArray, JsObject, Json}
import scalaj.http.{Http, HttpRequest}

import scala.annotation.tailrec
import scala.util.{Success, Try}

object Wikifier extends NERClassifier{

  val wikifierToken: String = sys.env("wikifierToken")

  private case class Annotation(text: String, types: List[String])

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
          ((ann \ "support").as[JsArray], (ann \ "dbPediaTypes").as[List[String]])
        )
        .flatMap{
          case (supports, types) =>
            val mostConfident = supports.value
              .map(sup => ((sup \ "chFrom").as[Int], (sup \ "chTo").as[Int], (sup \ "prbConfidence").as[Double], types))
              .maxBy(_._3)

            if(mostConfident._3 > 0.6)
              List(Annotation(text.substring(mostConfident._1, mostConfident._2+1), types))
            else Nil
        }

//    println(annotations.mkString("\n"))

    annotations
      .map(a => (a.text, a.types))
      .filterNot(_._2.isEmpty)
      .map(t => (t._1, List(t._2.last)))
      .toList
  }

  def main(args: Array[String]): Unit = {
    println(classify("Which Pope Succeeded John Paul II?").mkString("\n"))
  }

}
