package br.ufc.insightlab.linkedgraphast.query.closure

import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.model.link.{Attribute, Relation}

object ClosureHelper {

  def adjustEdgesWeight(graph: LinkedGraph): Unit = {
//    var counts = Map[String, Int](
//      "schema/owl" -> 0,
//      "subClass" -> 0,
//      "sub" -> 0,
//      "label" -> 0
//    )
    graph.getLinksAsStream
      .foreach(link => link match {
        case Relation(_, l, t) =>
          if ((l.uri.endsWith("#type") && (t.uri.endsWith("#Class") || t.uri.endsWith("Property"))) ||
            (l.uri.endsWith("#range") && t.uri.contains("XMLSchema#")) ||
            l.uri.contains("/owl#")
          ) {
            link.setWeight(100)
//            counts += "schema/owl" -> (counts("schema/owl") + 1)
          }
          else if (l.uri.contains("rdf-schema#subClassOf")) {
            link.setWeight(5)
//            counts += "subClass" -> (counts("subClass") + 1)
          }
          else if (l.uri.contains("rdf-schema#sub")) {
            link.setWeight(100)
//            counts += "sub" -> (counts("sub") + 1)
          }

        case Attribute(_, l, _) =>
          if (l.uri.endsWith("#label")) {
            link.setWeight(10)
//            counts += "label" -> (counts("label") + 1)
          }

        case _ =>
      })
//    println("\n------------------------\n")
//    println(counts.mkString("\n"))
//    println("\n------------------------\n")

  }
}
