package com.dsouzam.language_analysis

import java.sql.Timestamp
import java.time.LocalDateTime

import com.dsouzam.githubapi._
import com.dsouzam.language_analysis.tables.Repositories
import de.tudarmstadt.ukp.jwktl.api.PartOfSpeech
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.Duration


class Extractor(val client: APIClient, val database: Database) {
  val RANGE = 3
  val ranker = new TextRank(RANGE, Set(PartOfSpeech.NOUN))
  val repositories = TableQuery[Repositories]

  // queries GitHub and updates database with results
  def update(language: String, num: Int) = {
    val desiredCount = num.toInt
    val currentCount = getOffset(language)
    if (currentCount < desiredCount) {
      println(s"$currentCount repositories found for $language. Fetching ${desiredCount-currentCount} more.")

      val repos = extract(language, desiredCount, currentCount)
      val inserts = generateInserts(repos:_*)
      val query = repositories ++= inserts
      Await.result(database.run(query), Duration.Inf)
    } else {
      println(s"$currentCount repositories found for $language. Nothing to do.")
    }
  }

  // queries GitHub in sets of 10 and concatenates the result
  def extract(language: String, count: Int, offset: Int): Seq[Repository] = {
    val PER_PAGE = 10
    val startPage = offset/PER_PAGE+1
    val endPage = (count+PER_PAGE-1)/PER_PAGE
    val query = new RepositorySearchQueryBuilder("")
      .language(language)
      .perPage(PER_PAGE)
      .sortBy("stars")
    (startPage to endPage).flatMap { pageNumber =>
      query.getPage(pageNumber)
      client.searchRepos(query.build, withLanguages = true, withReadMe = true)
    }
  }

  private def toTs(ldt: LocalDateTime) = Timestamp.valueOf(ldt)
  private def now = new Timestamp(System.currentTimeMillis())
  private def keywords(repository: Repository) = {
    val str = if (repository.readMe.nonEmpty) repository.readMe else repository.description
    ranker.rank(str).take(5).map(_._1).mkString(",")
  }

  def generateInserts(repositories: Repository*)= {
    repositories.map{ r =>
      val topLang+:otherLangs = r.languages
        .toSeq
        .sortBy(_._2)
        .map(_._1)
        .reverse
      (r.url, r.name, r.id, r.description, r.readMe, topLang, otherLangs.mkString(","), toTs(r.createdAt),
        toTs(r.updatedAt), toTs(r.pushedAt), r.stars, r.watchers, r.hasPages, r.forks, r.defaultBranch, now, keywords(r))
    }
  }

  // counts how many records exist for a given language
  def getOffset(language: String): Int = {
    val query = repositories.filter(_.language === language).length.result
    Await.result(database.run(query), Duration.Inf)
  }
}
