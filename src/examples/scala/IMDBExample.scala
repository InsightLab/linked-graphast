import br.ufc.insightlab.linkedgraphast.modules.fragmentexpansor.FragmentExpansor
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.SimilarityKeywordMatcherOptimizedWithFilters
import br.ufc.insightlab.linkedgraphast.modules.keywordmatcher.similarity.{JaroWinkler, PermutedSimilarity}
import br.ufc.insightlab.linkedgraphast.modules.querybuilder.SchemaSPARQLQueryBuilder
import br.ufc.insightlab.linkedgraphast.parser.NTripleParser
import br.ufc.insightlab.linkedgraphast.query.steinertree.SteinerTree

object IMDBExample extends App{
  val graph = NTripleParser.parse("src/evaluation/resources/MovieOntology.nt")

  val s = "movie with title[=;Finding Nemo] genre, production start year, budget, gross, imdb rating, that has actor with birth name, has director with birth name, has producer with birth name, has editor with birth name"

  val (nodes,filters) = new SimilarityKeywordMatcherOptimizedWithFilters(new PermutedSimilarity(JaroWinkler))(graph)(s)
  println(s"Nodes: \n${nodes.mkString("\n")} \nFilters: \n${filters.mkString("\n")}")

  val fragment = SteinerTree(graph)(nodes.toList)

  println("\nFragment:")
  fragment.getLinksAsStream.foreach(println)

  println("\nSuggestions")
  val suggestions = FragmentExpansor(graph)(fragment)
  println(suggestions mkString ",")

  println("Query:")
  val query = SchemaSPARQLQueryBuilder(fragment,filters, graph)

  println(query)
}

