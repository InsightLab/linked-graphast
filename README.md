# Linked-Graphast

Linked Graphast is a framework built over [Graphast](https://github.com/InsightLab/graphast), a framework to manipulate graphs and time dependent networks.

The main purpose of Linked Graphast is to represent and manipulate linked data from RDF Ontologies as a RDF Graph, allowing algorithms linke *Dijkstra*, *A\**, *Prim*, etc., to be applied over your data using a simple graph structure.

## Main Modules

Linked-Graphast contains some modules that can be used over the RDF ontology in *information retrieval* scenarios. An example can be found at [examples source folder ](https://github.com/InsightLab/linked-graphast/tree/master/src/examples/scala)
**WARN**: to better results, all entities in the ontology(classes and properties) must be **labeled**.

### Keyword Matcher

This module can process the keywords of a text and retrieve the nodes that match with the written text. It uses a Similarity Metric object to compute the similarity among the terms instead of doing an simple exact match.

### Fragment Extractor

This module can, given a text, identify the nodes that match with the text and give the minimum subgraph(fragment) from the ontology that connect these nodes solving the Steiner Tree problem.

### Query Builder

Given a Linked Graph, this module can create a SPARQL query representing that fragment.
**OBS**: this module works best using only the ontology schema.

## Neo4j integration

To store large graphs, Linked-Graphast contains a Graph Structure implemented over [Neo4j](https://neo4j.com/). Its only a prototype, but it works well on static graph storages.

## Java Integration

Since Linked-Graphast is made on Scala, it can be used on a Java project. However, it uses a lot of features from Scala that aren't compatible with Java(8) like currying, high order functions, use of Object as paramethers, etc. There are 2 modules implemented on [VonQBE package](https://github.com/InsightLab/linked-graphast/tree/master/src/main/scala/br/ufc/insightlab/linkedgraphast/modules/vonqbe) that simplifies the method calls to Java. To better use of Linked-Graphast on a Java application, maybe is better to fork this project and implement the modules according to your needs.
