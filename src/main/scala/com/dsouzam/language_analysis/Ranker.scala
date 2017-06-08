package com.dsouzam.language_analysis

import java.io.{File, PrintWriter}

import com.dsouzam.githubapi.Repository
import com.dsouzam.language_analysis.tables.Repositories
import de.tudarmstadt.ukp.jwktl.api.PartOfSpeech
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits._

class Ranker(val database: Database, range: Int = 3) {
  val textRanker = new TextRank(range, Set(PartOfSpeech.NOUN))
  val repositoriesTable = TableQuery[Repositories]

  private def keywords(repository: Repository) = {
    val str = if (repository.readMe.nonEmpty) repository.readMe else repository.description
    textRanker.rank(str)
  }

  private def printable(id: Int, keywords: Seq[(String, Float)]) = {
    val keywordString = keywords.map( tup => s"${tup._1} ${tup._2}").mkString(",")
    s"$id|$keywordString"
  }

  def run() = {
    val CHUNK_SIZE = 100
    val countAction = repositoriesTable.length.result
    val count = Await.result(database.run(countAction), Duration.Inf)

    val file = new File("keywords.txt")
    val writer = new PrintWriter(file)
    try {
      (0 to count by CHUNK_SIZE).foreach( (start: Int) => {
        print(s"Ranking keywords for repositories ${start+1} through ${start+CHUNK_SIZE}...")
        val chunkAction = repositoriesTable.map(r => (r.id, r.readMe)).drop(start).take(CHUNK_SIZE).result
        val chunk = Await.result(database.run(chunkAction), Duration.Inf)

        val lines = chunk.flatMap {
          case (_, "") => None // empty readMe, ignore
          case (id, readMe) => print("."); Some(printable(id, textRanker.rank(readMe)))
        }

        lines.foreach(writer.println)
        println("done.")
      })
    } finally {
      writer.close()
    }

  }
}
