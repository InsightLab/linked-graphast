package br.ufc.insightlab.linkedgraphast.modules.fragmentexpansor

import scala.collection.JavaConverters._
import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.model.link.{Attribute, Link}
import br.ufc.insightlab.linkedgraphast.model.node.Literal

object FragmentExpansor {

  def apply(graph: LinkedGraph)(f: LinkedGraph): List[String] = {

    require(f.getNumberOfNodes > 0)

    val firstNode = f.getNodes.iterator.next

    var fragment = new LinkedGraph()

    if(f.getNumberOfNodes == 1 && firstNode.isInstanceOf[Literal])
      graph.getInEdges(firstNode.getId).asScala
        .map(_.asInstanceOf[Link])
        .filter(l => l.uri.uri.endsWith("#label") && l.target==firstNode)
        .foreach(l => {fragment.addNode(l.source)})
    else fragment = f


    val nodes = graph.getLinksAsStream
      .filter(l => {
          !fragment.hasLink(l) &&
            (fragment.containsNode(l.source.getId) || fragment.containsNode(l.target.getId)) &&
            !(l.uri.uri.endsWith("#subClassOf") || l.target.value.endsWith("#Class") || l.target.value.contains("XMLSchema") || l.target.value.endsWith("Property") || l.target.isInstanceOf[Literal])
      })
      .map(l => if(fragment.containsNode(l.source.getId)) l.target else l.source)
      .toSet


    var suggestions: List[String] = nodes.
      flatMap(n => graph.getInEdges(n.getId).asScala
        .filter(l => l.isInstanceOf[Attribute] && l.asInstanceOf[Attribute].uri.uri.endsWith("#label"))
        .map(_.asInstanceOf[Attribute].value.split("@").head)
      ).toList

    suggestions
  }

}
