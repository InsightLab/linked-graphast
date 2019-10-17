package br.ufc.insightlab.linkedgraphast.modules

package object keywordmatcher {

  private val puntcuation: Set[Char] = Set('?','!')

  implicit class RichString(val s: String) extends AnyVal {
    def removePunctuation: String = s.filterNot(c => puntcuation(c))
  }

}
