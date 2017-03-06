package com.dsouzam.language_analysis

import de.tudarmstadt.ukp.jwktl.api.PartOfSpeech

import scala.collection.mutable

// range: the maximum number of words away two words can be and still be adjacent.
// partsOfSpeech: the parts of speech to consider when extracting keywords
class TextRank(range: Int, partsOfSpeech: Set[PartOfSpeech]) {
  private val THRESHOLD = 0.0001
  // Filters tokens that do not match any of partsOfSpeech
  private def accepted(token: Token) = token.partsOfSpeech.intersect(partsOfSpeech).nonEmpty

  // Generates the nodes of an adjacency graph and their neighbours
  private def computeGraph(tokens: IndexedSeq[Token]): Seq[Node] = {
    val nodes = mutable.Map[String, Node]()
    tokens.zipWithIndex.foreach{ case (token: Token, i: Int) => {
      if (accepted(token)) {
        val node = nodes.getOrElseUpdate(token.word, Node(token.word))
        val min = math.max(0, i - range)
        val max = math.min(tokens.length, i + range + 1)

        // get neighbouring accepted words
        val neighbouringTokens = tokens.slice(min, max).filter((neighbour: Token) => accepted(neighbour) && neighbour != token)
        val neighbours = neighbouringTokens.map((token: Token) => nodes.getOrElseUpdate(token.word, Node(token.word)))

        node.addNeighbours(neighbours)
      }
    }}
    nodes.values.toSeq
  }

  // Returns sequence of keyword-score pairs for the input string, in order of descending score
  def rank(string: String): Seq[(String, Int)] = {
    val tokens = Tokenizer.tokenize(string)
    val nodes = computeGraph(tokens)

    //nodes.foreach{(node: Node) => println(s"${node.word}: ${node.getNeighbours.map(_.word).mkString(", ")}")}



    Seq()
  }
}

case class Node(word: String) {
  private val neighbours = mutable.Set[Node]()
  private var score = 1


  def addNeighbour(newNeighbour: Node) = neighbours += newNeighbour
  def addNeighbours(newNeighbours: Seq[Node]) = newNeighbours.foreach(addNeighbour)
  def getNeighbours = neighbours.toSet // prevent mutation

  def setScore(newScore: Int) = score = newScore
  def getScore = score
}