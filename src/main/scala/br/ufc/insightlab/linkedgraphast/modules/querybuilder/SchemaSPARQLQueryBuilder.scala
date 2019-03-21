package br.ufc.insightlab.linkedgraphast.modules.querybuilder

import org.apache.jena.graph.Triple
import org.apache.jena.query.{Query, QueryFactory}
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.sparql.core.Var
import org.apache.jena.sparql.expr._
import org.apache.jena.sparql.expr.nodevalue.NodeValueString
import org.apache.jena.sparql.syntax.{ElementFilter, ElementGroup, ElementOptional, ElementTriplesBlock}
import br.ufc.insightlab.linkedgraphast.model.graph.LinkedGraph
import br.ufc.insightlab.linkedgraphast.model.helper.LinkedNodeHelper
import br.ufc.insightlab.linkedgraphast.model.link.{Attribute, Link, Relation}
import br.ufc.insightlab.linkedgraphast.model.node.{LinkedNode, Literal, URI}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object SchemaSPARQLQueryBuilder {

  private val log = LoggerFactory.getLogger(this.getClass)

  private val model = ModelFactory.createDefaultModel()

  private var URIToVar: Map[String, Var] = Map.empty

  private def getVar(URI: String, block: ElementTriplesBlock = null, prefix: String = ""): Var = {
    val uri = prefix + cleanURI(URI)
    if (URIToVar.contains(uri)) URIToVar(uri)
    else {
      val v = Var.alloc(uri)
      URIToVar += uri -> v
      if (null != block)
        block.addTriple(new Triple(v, model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type").asNode, model.createResource(URI).asNode))

      v
    }
  }

  private def hasVar(URI: String): Boolean =
    URIToVar.contains(cleanURI(URI))

  def cleanURI(uri: String): String =
    uri.reverse.takeWhile(c => c != '/' && c != '#').reverse.replace(">", "").replace(".", "")

  private[querybuilder] def processSingleElement(g: LinkedGraph, filtersMap: Map[Long, List[String]] = Map.empty, schema: LinkedGraph, query: Query): ElementGroup = {
    val block = new ElementTriplesBlock()
    val body = new ElementGroup()

    g.getNodes.iterator.next match {
      case literal: Literal =>
        val nodes = schema.getInEdges(literal.getId).asScala
          .map(_.asInstanceOf[Link])
          .filter(l => l.uri.uri.endsWith("#label") && l.target == literal)
          .map(_.source).toStream
          .filter(isClass(_, schema))

        if (nodes.nonEmpty) {
          val node = nodes.head

          if (isClass(node, schema)) {
            val s = getVar(node.uri, block)
            query.addResultVar(s)

            if (filtersMap.getOrElse(literal.getId, List()).nonEmpty) {
              query.addResultVar(s)

              val o = getVar(s.getVarName + "_value")
              query.addResultVar(o)

              val p = getVar(s.getVarName + "_property")
              query.addResultVar(p)

              val pattern = new Triple(s, p, o)
              block.addTriple(pattern)

              //      for(property <- getDataTypeProperties(uri,schema)){
              //        val p = model.createProperty(property).asNode
              //
              //        val pattern = new Triple(s, p, o)
              //        val optBlock = new ElementTriplesBlock()
              //        optBlock.addTriple(pattern)
              //        body.addElement(new ElementOptional(optBlock))
              //      }

              for {
                f <- filtersMap.getOrElse(literal.getId, List())
              } {
                val filterElement = generateStringFilter(o, f)
                if (filterElement.isDefined)
                  body.addElement(filterElement.get)
              }
            }


          }
          else if (isDataTypeProperty(node.value, schema)) {
            val s = getVar("xpto")
            query.addResultVar(s)

            val p = model.createProperty(node.value).asNode

            val o = getVar(node.value, prefix = s.getVarName + "_")
            query.addResultVar(o)

            val pattern = new Triple(s, p, o)
            block.addTriple(pattern)

            for (filter <- filtersMap.getOrElse(literal.getId, List())) {
              val filterElement = generateFilter(literal.value, o, filter, schema)
              if (filterElement.isDefined)
                body.addElement(filterElement.get)
            }
          }

        }

      case _ =>
    }

    body.addElement(block)
    //    query.setQueryPattern(body)
    //
    //    query.toString

    body
  }

  private[querybuilder] def isClass(node: LinkedNode, schema: LinkedGraph): Boolean = {
    schema.getInEdges(node.getId)
      .asScala
      .map(_.asInstanceOf[Link])
      .exists(l => l.uri.uri.endsWith("#type") && l.target.value.endsWith("#Class"))
  }

  private[querybuilder] def processMultipleElements(g: LinkedGraph, filtersMap: Map[Long, List[String]] = Map.empty, schema: LinkedGraph, query: Query): ElementGroup = {


    var URIfilters = Map[String, List[String]]()

    val block = new ElementTriplesBlock()
    val body = new ElementGroup()

    val graph = compressSubclasses(g, filtersMap)

//    println("Compressed fragment:")
//    println(graph.linksAsString()+"\n\n")

    graph.getLinksAsStream.foreach{
      case Attribute(s,_,o) =>
        if(filtersMap.contains(o.getId)){
          URIfilters += s.uri -> filtersMap(o.getId)
        }

      case _ =>
    }

    var domainsMap = Map[String, List[String]]()
    var rangesMap = Map[String, List[String]]()

    val properties = graph.getLinksAsStream.filter {
      case Relation(s, p, o) => p.uri.endsWith("#domain") || p.uri.endsWith("#range")
      case _ => false
    }.map { case Relation(s, p, o) => (s.uri, p.uri, o.uri) }
      .groupBy(_._1)
      .map { case (p, ls) => p -> ls.map(x => (x._2, x._3)).groupBy(_._1) }
    //    println(properties.mkString("\n")+"\n")

    if (properties.nonEmpty) {
      val schemaTriples: List[Relation] = properties.toList.flatMap { case (p, ls) => {
        val domainURI: Option[String] =
          Try(ls.keySet.filter(_.endsWith("#domain")).head) match {
            case Success(s) => Some(s)
            case _ => None
          }

        val rangeURI: Option[String] = Try(ls.keySet.filter(_.endsWith("#range")).head) match {
          case Success(s) => Some(s)
          case _ => None
        }

        if (domainURI.isDefined) {
          if (rangeURI.isDefined) {
            val domains = ls(domainURI.get).map(_._2)
            val ranges = ls(rangeURI.get).map(_._2)

            for {
              d <- domains
              r <- ranges
              if r != d
            } yield Relation(URI(d), URI(p), URI(r))
          }
          else {
            domainsMap += p -> domainsMap.getOrElse(p, Nil).union(ls(domainURI.get).map(_._2))
            Nil
          }
        }
        else {
          if (rangeURI.isDefined) {
            rangesMap += p -> rangesMap.getOrElse(p, Nil).union(ls(rangeURI.get).map(_._2))
          }
          Nil
        }
      }

      }

      for (triple <- schemaTriples) {
        val s = getVar(triple.source.uri, block)
        query.addResultVar(s)

        val p = model.createProperty(triple.uri.uri).asNode

        val o = getVar(triple.target.value, block)
        query.addResultVar(o)

        val pattern = new Triple(s, p, o)
        block.addTriple(pattern)
      }
    }

//    println(rangesMap)
//    println(domainsMap)

    for {
      (property, rangeList) <- rangesMap
    } {
      val s = getVar(property)
      query.addResultVar(s)

      val p = model.createProperty(property).asNode

      for(range <- rangeList){
        val o = getVar(range, block)
        query.addResultVar(o)

        val pattern = new Triple(s, p, o)
        block.addTriple(pattern)
      }

    }

    for {
      (property, subjects) <- domainsMap
      if isDataTypeProperty(property, schema)
      subject <- subjects
    } {
      val s = getVar(subject, block)
      query.addResultVar(s)

      val p = model.createProperty(property).asNode

      val o = getVar(property, prefix = s.getVarName + "_")
      query.addResultVar(o)

      val pattern = new Triple(s, p, o)
      block.addTriple(pattern)

      for (filter <- URIfilters.getOrElse(property, List())) {
        val filterElement = generateFilter(property, o, filter, schema)
        if (filterElement.isDefined)
          body.addElement(filterElement.get)
      }

      URIfilters -= property
    }


    for {
      (property, subjects) <- domainsMap
      if !isDataTypeProperty(property, schema)
      subject <- subjects
      range <- getRanges(property, schema)
      //      if hasVar(range)
    } {
      val s = getVar(subject, block)
      query.addResultVar(s)

      val p = model.createProperty(property).asNode

      //      print((range,subject))
      val o = if (range == subject) {
        val o = getVar(range + "_2")
        query.addResultVar(o)
        o
      } else {
        val o = getVar(range)
        query.addResultVar(o)
        o
      }

      val pattern = new Triple(s, p, o)
      block.addTriple(pattern)
    }

    println(URIfilters.mkString("\n"))
    for {
      (uri, filters) <- URIfilters
      if isClass(schema.getNodeByURI(uri).asInstanceOf[LinkedNode], schema)
    } {
      val s = getVar(uri)
      query.addResultVar(s)

      val o = getVar(s.getVarName + "_value")
      query.addResultVar(o)

      val p = getVar(s.getVarName + "_property")
      query.addResultVar(p)

      val pattern = new Triple(s, p, o)
      block.addTriple(pattern)
      body.addElement(new ElementFilter(new E_IsLiteral(new ExprVar(o))))

      //      for(property <- getDataTypeProperties(uri,schema)){
      //        val p = model.createProperty(property).asNode
      //
      //        val pattern = new Triple(s, p, o)
      //        val optBlock = new ElementTriplesBlock()
      //        optBlock.addTriple(pattern)
      //        body.addElement(new ElementOptional(optBlock))
      //      }

      for (f <- filters) {
        val filterElement = generateStringFilter(o, f)
        if (filterElement.isDefined)
          body.addElement(filterElement.get)
      }
    }

    body.addElement(block)
    //    query.setQueryPattern(body)
    //
    //    query.toString

    body
  }

  def apply(g: LinkedGraph, filtersMap: Map[Long, List[String]] = Map.empty, schema: LinkedGraph): String = {

    URIToVar = Map.empty
    val query = QueryFactory.make()
    query.setQuerySelectType()

    val body = if (g.getNumberOfNodes == 1) {
      processSingleElement(g, filtersMap, schema, query)
    }
    else {
      processMultipleElements(g, filtersMap, schema, query)
    }

    query.setQueryPattern(body)
    query.toString
  }

  private[querybuilder] def getDataTypeProperties(uri: String, schema: LinkedGraph): Seq[String] = {
    schema.getInEdges(LinkedNodeHelper.getNodeIdByURI(uri))
      .asScala
      .flatMap {
        case l: Link =>
          if (l.uri.uri.contains("#domain") && l.target.value == uri)
            List[String](l.source.uri)
          else if (l.uri.uri.contains("subClassOf") && l.source.uri == uri) {
            //            println(s"$uri is subclass of ${l.target.value}")
            getDataTypeProperties(l.target.value, schema)
          }
          else Nil
        case _ => Nil
      }
      .filter(isDataTypeProperty(_, schema))
      .toList

  }


  private[querybuilder] def generateStringFilter[querybuilder](v: Var, filter: String): Option[ElementFilter] = {
    val cleanFilter = filter
      .replace("[", "")
      .replace("]", "")

    try {
      val operation = cleanFilter.split(";").head
      val value = cleanFilter
        .replace(operation + ";", "")
        .toLowerCase

      operation.toLowerCase match {
        case "contains" =>
          Some(new ElementFilter(new E_StrContains(new E_StrLowerCase(
            new E_Str(new ExprVar(v))), new NodeValueString(value))))

        case "starts" =>
          Some(new ElementFilter(new E_StrStartsWith(new E_StrLowerCase(
            new E_Str(new ExprVar(v))), new NodeValueString(value))))

        case "ends" =>
          Some(new ElementFilter(new E_StrEndsWith(new E_StrLowerCase(
            new E_Str(new ExprVar(v))), new NodeValueString(value))))

        case "=" =>
          //          Some(new ElementFilter(new E_Equals(new E_StrLowerCase(
          //          new E_Str(new ExprVar(v))), new NodeValueString(value))))
          Some(new ElementFilter(
            new E_Regex(new E_StrLowerCase(new E_Str(new ExprVar(v))), "^" + value + "$", "i")))

        case "!=" => Some(new ElementFilter(new E_NotEquals(new E_StrLowerCase(
          new E_Str(new ExprVar(v))), new NodeValueString(value))))

        case ">" => Some(new ElementFilter(new E_GreaterThan(new E_StrLowerCase(
          new E_Str(new ExprVar(v))), new NodeValueString(value))))

        case ">=" => Some(new ElementFilter(new E_GreaterThanOrEqual(new E_StrLowerCase(
          new E_Str(new ExprVar(v))), new NodeValueString(value))))

        case "<" => Some(new ElementFilter(new E_LessThan(new E_StrLowerCase(
          new E_Str(new ExprVar(v))), new NodeValueString(value))))

        case "<=" => Some(new ElementFilter(new E_LessThanOrEqual(new E_StrLowerCase(
          new E_Str(new ExprVar(v))), new NodeValueString(value))))

        case _ =>
          log.warn(s"Filter $operation not recognized")
          None
      }

    }
    catch {
      case e: Exception =>
        log.error(e.getMessage)
        log.warn(s"Bad formated filter: $filter. Filters must be like [operation;value]")
        None
    }
  }

  private[querybuilder] def generateNumberFilter[querybuilder](v: Var, filter: String, typeURI: String): Option[ElementFilter] = {
    val cleanFilter = filter
      .replace("[", "")
      .replace("]", "")

    try {
      val operation = cleanFilter.split(";").head
      val stringValue = cleanFilter
        .replace(operation + ";", "")
        .toLowerCase

      val value: Expr =
        if (typeURI.contains("#int"))
          Try(stringValue.toInt) match {
            case Success(i) => new NodeValueString(stringValue, model.createTypedLiteral(i, typeURI).asNode())
            case Failure(_) =>
              log.warn(s"Bad formatted value $stringValue to type $typeURI. Creating string filter")
              null
          }
        else if (typeURI.endsWith("#decimal") || typeURI.endsWith("#double"))
          Try(stringValue.toDouble) match {
            case Success(i) => new NodeValueString(stringValue, model.createTypedLiteral(i, typeURI).asNode())
            case Failure(_) =>
              log.warn(s"Bad formatted value $stringValue to type $typeURI. Creating string filter")
              null
          }
        else
          null

      if (null == value) generateStringFilter(v, filter)
      else
        operation.toLowerCase match {

          case "=" => Some(new ElementFilter(new E_Equals(new ExprVar(v), value)))

          case "!=" => Some(new ElementFilter(new E_NotEquals(
            new ExprVar(v), value)))

          case ">" => Some(new ElementFilter(new E_GreaterThan(
            new ExprVar(v), value)))

          case ">=" => Some(new ElementFilter(new E_GreaterThanOrEqual(
            new ExprVar(v), value)))

          case "<" => Some(new ElementFilter(new E_LessThan(
            new ExprVar(v), value)))

          case "<=" => Some(new ElementFilter(new E_LessThanOrEqual(
            new ExprVar(v), value)))

          case _ =>
            log.warn(s"Filter $operation not recognized")
            None
        }

    }
    catch {
      case e: Exception =>
        log.error(e.getMessage)
        log.warn(s"Bad formated filter: $filter. Filters must be like [operation;value]")
        None
    }
  }

  private[querybuilder] def generateFilter(uri: String, v: Var, filter: String, schema: LinkedGraph): Option[ElementFilter] = {
    val ranges = getRanges(uri, schema)
    if (ranges.exists(r => r.contains("#int") || r.endsWith("#decimal") || r.endsWith("#double")))
      generateNumberFilter(v, filter, ranges.head)
    else
      generateStringFilter(v, filter)
  }

  def compressSubclasses(graph: LinkedGraph, filtersMap: Map[Long, List[String]]): LinkedGraph = {
    var superClasses: Set[LinkedNode] = Set()
    var newLinks: List[Link] = Nil

    do {
      newLinks = List[Link]()
      superClasses = Set[LinkedNode]()

      graph.getLinksAsStream
        .foreach {
          link =>

            if (link.uri.value.endsWith("subClassOf")) {
              val subClass = link.source
              val superClass = link.target.asInstanceOf[URI]

              //checking if there is another superclass
              if (!graph.getInEdges(superClass.getId).asScala.map(_.asInstanceOf[Link]).exists(l => l.uri.uri.endsWith("subClassOf") && l.source == superClass)) {
                superClasses += superClass
                graph.getInEdges(superClass.getId).asScala.foreach {
                  case l: Link =>
                    if (!l.uri.value.endsWith("subClassOf")) l match {
                      case r: Relation =>
                        if (r.source == superClass)
                          newLinks :+= Relation(subClass, r.uri, r._target)
                        else if (r._target == superClass)
                          newLinks :+= Relation(r.source, r.uri, subClass)

                      case a: Attribute => newLinks :+= Attribute(subClass, a.uri, a._value)
                    }

                  case _ =>

                }
              }
            }
        }

      for (l <- newLinks) graph.addLink(l)
      for {
        n <- superClasses
        if !filtersMap.contains(n.getId)
      } graph.removeNode(n.getId)

    } while (newLinks.nonEmpty)

    graph
  }

  private[querybuilder] def isDataTypeProperty(uri: String, schema: LinkedGraph): Boolean = {
    val node = schema.getNodeByURI(uri)

    !schema.getInEdges(node.getId).asScala
      .map(_.asInstanceOf[Link])
      .exists(l =>
        l.uri.uri.endsWith("#range") && !l.target.value.contains("XMLSchema#") && !l.target.value.endsWith("#PlainLiteral") && !l.target.value.endsWith("#langString"))
  }

  private[querybuilder] def getRanges(uri: String, schema: LinkedGraph): List[String] =
    schema.getInEdges(schema.getNodeByURI(uri).getId)
      .asScala
      .map(_.asInstanceOf[Link])
      .filter(_.uri.uri.endsWith("#range"))
      .map(_.target.value)
      .toList

  private[querybuilder] def getDomains(uri: String, schema: LinkedGraph): List[String] =
    schema.getInEdges(schema.getNodeByURI(uri).getId)
      .asScala
      .map(_.asInstanceOf[Link])
      .filter(_.uri.uri.endsWith("#domains"))
      .map(_.target.value)
      .toList
}
