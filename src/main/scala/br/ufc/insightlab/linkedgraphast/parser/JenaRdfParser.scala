package br.ufc.insightlab.linkedgraphast.parser
import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.model.link.{Attribute, Relation}
import br.ufc.insightlab.linkedgraphast.model.node.{Literal, URI}
import org.apache.jena.rdf.model.{ModelFactory, Resource}

import scala.collection.JavaConverters._

object JenaRdfParser extends RDFParser {
  override def parse(filePath: String, graph: LinkedGraph = new LinkedGraph()): LinkedGraph = {
    val model = ModelFactory.createDefaultModel()
    model.read(filePath)

    model.listStatements().asScala.foreach{stm =>
      val subject = URI(stm.getSubject.getURI)
      val predicate = URI(stm.getPredicate.getURI)
      val obj = stm.getObject match {
        case r: Resource => URI(r.getURI)
        case l: org.apache.jena.rdf.model.Literal => Literal(l.getString+"@"+l.getLanguage)
      }

      if(!graph.containsNode(subject.getId))
        graph.addNode(subject)
      if(!graph.containsNode(predicate.getId))
        graph.addNode(predicate)
      if(!graph.containsNode(obj.getId))
        graph.addNode(obj)

      obj match {
        case u: URI => graph.addLink(Relation(subject, predicate, u))
        case l: Literal => graph.addLink(Attribute(subject, predicate, l))
      }

    }

    graph
  }
}
