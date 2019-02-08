package br.ufc.insightlab

import br.ufc.insightlab.ror.implementations.OntopROR

object RoRTest extends App{
  val ror = new OntopROR(
    "src/evaluation/resources/MovieOntology.owl",
    "src/evaluation/resources/mapping.obda")

  val query =
    """
      SELECT  ?TVSeries
      WHERE
        {
          ?TVSeries  a                    <http://www.movieontology.org/2009/10/01/movieontology.owl#TVSeries> .
       } limit 5

    """.stripMargin

  ror.runQuery(query).iterator()
}
