package br.ufc.insightlab.linkedgraphast.query.mst

import scala.collection.JavaConverters._

import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import org.insightlab.graphast.model.{Edge, Node}

import scala.collection.mutable.PriorityQueue

object Prim {

  def apply(graph: LinkedGraph)(start: Node): LinkedGraph = {
    val mst = new LinkedGraph

    var pi: Map[Long, Long] = Map.empty

    var q = PriorityQueue[(Node, Double)]()(Ordering.by(_._2))
    q.enqueue((graph.getNodes.iterator().next(), 0))

    while(q.nonEmpty) {
      val (v,_) = q.dequeue()
      mst.addNode(v)

      for(n <- graph.getNeighborhood(v.getId).asScala.map(_.asInstanceOf[Long])){
        if(!mst.containsNode(n))
          if(!pi.contains(n) ||
            graph.getEdge(pi(n),n).getWeight > graph.getEdge(v.getId,n).getWeight)
          {
            q = q.filter(_._1 != n)
            q.enqueue((graph.getNode(n),graph.getEdge(v.getId,n).getWeight))
            pi += (n -> v.getId)
          }
      }
    }

    for((src,trg) <- pi)
      graph.getEdge(src,trg) match {
        case e: Edge =>
          mst.addEdge(e)

        case _ =>
          val e = graph.getEdge(trg, src)
          mst.addEdge(e)
      }

    mst
  }

}

