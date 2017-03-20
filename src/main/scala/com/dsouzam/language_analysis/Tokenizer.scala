package com.dsouzam.language_analysis

import scala.collection.JavaConversions._
import java.io.File

import de.tudarmstadt.ukp.jwktl.JWKTL
import de.tudarmstadt.ukp.jwktl.api.{IWiktionaryEntry, PartOfSpeech}

import scala.collection.mutable

case class Token(word: String, partsOfSpeech: Set[PartOfSpeech])

object Tokenizer {
  val dict = JWKTL.openEdition(new File("res/wiktionary"))
  val cachedResults = mutable.Map[String, Set[PartOfSpeech]]()

  private def isLanguage(lang: String)(entry: IWiktionaryEntry): Boolean = entry.getWordLanguage.getCode == lang

  private val whitespace = "\\s".r
  private val trimmer = "(^\\W*|\\W*$)".r // non-alphanumeric characters at word boundaries

  // this can be fine-tuned to see what number gives good parts-of-speech results
  private val RELEVANT_ENTRIES = 1

  private def split(string: String): Seq[String] = whitespace.split(string)
  private def trim(word: String): String = trimmer.replaceAllIn(word, "")

  def getToken(word: String): Token = {
    assert(whitespace.findFirstIn(word).isEmpty, "Word cannot contain whitespace.")
    val entries = cachedResults.getOrElseUpdate(word,dict.getEntriesForWord(word)
      .toList
      .filter(isLanguage("eng"))
      .take(RELEVANT_ENTRIES)
      .map(_.getPartOfSpeech)
      .toSet
    )

    Token(word, entries)
  }

  def tokenize(string: String): IndexedSeq[Token] = {
    split(string.toLowerCase)
      .map(trim)
      .filter(_.nonEmpty)
      .map(getToken)
      .toIndexedSeq
  }
}
