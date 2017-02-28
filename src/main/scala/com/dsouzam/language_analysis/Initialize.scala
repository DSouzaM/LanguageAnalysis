package com.dsouzam.language_analysis

import java.io.File
import de.tudarmstadt.ukp.jwktl.JWKTL

object Initialize {
  // Before running, place decompressed xml file in res/.
  // This function will parse the Wiktionary dump into an Oracle Berkeley DB so it can be more easily queried by JWKTL.
  // This process can take a while, even up to 2 hours (https://dkpro.github.io/dkpro-jwktl/documentation/getting-started/).
  def main(args: Array[String]): Unit = {
    val dump = new File("res/enwiktionary-latest-pages-articles.xml")
    val output = new File("res/wiktionary")
    val begin = System.nanoTime
    JWKTL.parseWiktionaryDump(dump, output, true)
    val end = System.nanoTime
    println(s"Done in ${(end-begin)/(1000*1000*1000)} s.")
  }
}
