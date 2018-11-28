package br.ufc.insightlab.linkedgraphast.parser

import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.model.link.{Attribute, Relation}
import br.ufc.insightlab.linkedgraphast.model.node.{Literal, URI}
import org.slf4j.LoggerFactory

import scala.io.Source

object NTripleParser extends RDFParser {

  private val log = LoggerFactory.getLogger(this.getClass)

  override def parse(filePath: String, graph: LinkedGraph = new LinkedGraph()): LinkedGraph = {

    val lines = Source.fromFile(filePath).getLines()

    while (lines.hasNext) {
      val line = lines.next

      if (line.length > 1) {
        val values = line.split("> ")

        if (values.size > 2) {
          val subjectURI = values(0).replace("<", "").replace(" ", "")
          val predicateURI = values(1).replace("<", "")

          val obj =
            if (values.size == 3) values(2).dropRight(2)
            else values.tail.tail.mkString("> ").dropRight(2)

          val subject =
            if (!graph.containsNode(subjectURI)) {
              val cp = URI(subjectURI)
              graph.addNode(cp)
              cp
            } else graph.getNodeByURI(subjectURI).asInstanceOf[URI]

          val predicate =
            if (!graph.containsNode(predicateURI)) {
              val cp = URI(predicateURI)
              graph.addNode(cp)
              cp
            } else graph.getNodeByURI(predicateURI).asInstanceOf[URI]

          val objValue =
            if (obj.startsWith("<")) URI(obj.tail.dropRight(1))
            else Literal(obj.replace("\"", ""))


          val objNode =
            if (!graph.containsNode(objValue.value)) {
              graph.addNode(objValue)
              objValue
            } else graph.getNodeByURI(objValue.value)

          objNode match {
            case u: URI =>
              val r = Relation(subject, predicate, u)
              graph.addLink(r)
              log.debug(s"Adding Relation $r")

            case l: Literal =>
              val a = Attribute(subject, predicate, l)
              graph.addLink(a)
              log.debug(s"Adding Attribute $a")
          }

        }
        else {
          log.warn(s"Bad formated triple: $line")
        }
      }

    }

    graph
  }

}

