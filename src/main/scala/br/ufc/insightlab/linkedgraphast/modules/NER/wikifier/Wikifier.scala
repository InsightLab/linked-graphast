package br.ufc.insightlab.linkedgraphast.modules.NER.wikifier

import br.ufc.insightlab.linkedgraphast.modules.NER.NERClassifier
import play.api.libs.json.{JsArray, JsObject, Json}
import scalaj.http.{Http, HttpOptions}

object Wikifier extends NERClassifier{

  lazy val wikifierToken: String = sys.env("wikifierToken")

  private case class Annotation(text: String, types: List[String])

  def classify(text: String): List[(String, List[String])] = {
    val request = Http("http://www.wikifier.org/annotate-article")
      .param("userKey",wikifierToken)
      .param("text", text)
      .option(HttpOptions.connTimeout(50000))
      .option(HttpOptions.readTimeout(50000))

    val response = request.asString.body

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