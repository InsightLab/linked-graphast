package experiments.helper

import scala.util.Random

object PermutationHelper {
  def charPermutation(text: String): String = text.split(" ").map(word => {
    if (Random.nextBoolean()) {
      val l = word.length
      val i = Random.nextInt(l)

      var finalWord = word.toArray

      finalWord(i) = Random.alphanumeric.filter(_.isLetter).head

      finalWord.mkString("")
    } else word
  }).mkString(" ")

  def wordPermutation(text: String): String = Random.shuffle(text.split(" ").toList).mkString(" ")

  def wordAndCharPermutation(text: String): String =
    charPermutation(wordPermutation(text))
}