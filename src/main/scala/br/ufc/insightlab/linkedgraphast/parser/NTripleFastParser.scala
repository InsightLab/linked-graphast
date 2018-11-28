package br.ufc.insightlab.linkedgraphast.parser

import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.model.link.{Attribute, Relation}
import br.ufc.insightlab.linkedgraphast.model.node.{Literal, URI}
import org.slf4j.LoggerFactory

import scala.io.Source

object NTripleFastParser {

  private def clearURI(URI: String): String =
    URI
      .replace("<", "")
      .replace(">","")
      .replace(" .","")


  private def clearLiteral(l: String): String =
    if(l.endsWith(" ."))
      l.replace("\"","").dropRight(2)
    else
      l.replace("\"","")

  private val log = LoggerFactory.getLogger(this.getClass)

  def parse(nodesPath: String, linksPath: String, graph: LinkedGraph = new LinkedGraph): LinkedGraph = {

    val nodes = Source.fromFile(nodesPath).getLines()

    nodes.foreach { id =>
      val node =
        if(id.startsWith("<"))
          URI(clearURI(id))
        else Literal(clearLiteral(id))
      graph.addNode(node)
    }

    log.debug("Nodes added")

    val lines = Source.fromFile(linksPath).getLines().toStream.par

    lines.foreach { line =>
      if (line.length > 1) {
        val values = line.split(" ")

        if (values.size > 2) {
          val subjectURI = clearURI(values(0))
          val predicateURI = clearURI(values(1))
          val obj =
            if (values(2).startsWith("<")) clearURI(values.tail.tail.mkString(" "))
            else clearLiteral(values.tail.tail.mkString(" "))
          val subject = graph.getNodeByURI(subjectURI).asInstanceOf[URI]

          val predicate = graph.getNodeByURI(predicateURI).asInstanceOf[URI]

          val objValue =
            if (obj.startsWith("<")) URI(clearURI(obj))
            else Literal(clearLiteral(obj))

          val objNode = graph.getNodeByURI(objValue.value)

          val link = objNode match {
            case u: URI =>
              Relation(subject, predicate, u)
            case l: Literal =>
              Attribute(subject, predicate, l)
          }

//          log.debug(s"Adding Link $link")
          graph.addLink(link)

        }
        else {
          log.warn(s"Bad formated triple: $line")
        }
      }

    }

    graph
  }
}