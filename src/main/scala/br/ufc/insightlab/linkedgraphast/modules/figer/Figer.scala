package br.ufc.insightlab.linkedgraphast.modules.figer

import edu.stanford.nlp.ling.CoreAnnotations.{SentencesAnnotation, TokensAnnotation}
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.util.{CoreMap, Pair, StringUtils}
import edu.washington.cs.figer.FigerSystem
import edu.washington.cs.figer.analysis.Preprocessing

import scala.collection.JavaConverters._

object Figer {

  private var initialized: Boolean = false
  var configFile: String = _
  private var sys: FigerSystem = _

  def init(file: String = configFile): Unit = {
    if(!initialized || file != configFile) {
      if(null == file) throw new Error("A configuration file must be informed to FIGER!")
      FigerSystem.configFile = file
      configFile = file
      sys = FigerSystem.instance()
      Preprocessing.initPipeline()
      initialized = true
      println("Figer initialized!")
    }
  }

  def classify(text: String): List[(String, List[String])] = {
    init()

    val annotation = new Annotation(text)
    Preprocessing.pipeline.annotate(annotation)

    (for ((sentence, sentId) <- annotation.get[java.util.List[CoreMap], SentencesAnnotation](classOf[SentencesAnnotation]).asScala.toList.zipWithIndex) yield {

//      System.out.println("[s" + sentId + "]tokenized sentence=" +
        StringUtils.joinWithOriginalWhiteSpace(sentence.get[java.util.List[CoreLabel], TokensAnnotation](classOf[TokensAnnotation]))
      val entityMentionOffsets = FigerSystem.getNamedEntityMentions(sentence)

      for (offset: Pair[Integer, Integer] <- entityMentionOffsets.asScala.toList) yield {

        val label = sys.predict(annotation, sentId, offset.first, offset.second)
        val aux: java.util.List[CoreLabel] = sentence.get[java.util.List[CoreLabel], TokensAnnotation](classOf[TokensAnnotation])
        val mention: String = StringUtils.joinWithOriginalWhiteSpace(aux.subList(offset.first, offset.second))

//        println("[s" + sentId + "]mention" + mention + "(" + offset.first + "," + offset.second + ") = " + mention + ", pred = " + label)
        (mention,label.split(",").toList.map(_.split("@").head.split("/").last))
      }
    }).flatten
  }

}
