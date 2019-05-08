package br.ufc.insightlab.linkedgraphast.query.closure

import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.model.link.{Attribute, Relation}

object ClosureHelper {

  def adjustEdgesWeight(graph: LinkedGraph): Unit =
    graph.getLinksAsStream
      .foreach(link => link match {
        case Relation(_, l, t) =>
          if ((l.uri.endsWith("#type") && (t.uri.endsWith("#Class") || t.uri.endsWith("Property"))) ||
            (l.uri.endsWith("#range") && t.uri.contains("XMLSchema#")) ||
            l.uri.contains("/owl#")
          )
            link.setWeight(100)
          else if (l.uri.contains("rdf-schema#subClassOf"))
            link.setWeight(5)
          else if (l.uri.contains("rdf-schema#sub"))
            link.setWeight(10)

        case Attribute(_, l, _) =>
          if(l.uri.endsWith("#label"))
            link.setWeight(10)

        case _ =>
      })

}
