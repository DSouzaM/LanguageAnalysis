package com.dsouzam.language_analysis

import java.sql.Timestamp
import java.time.LocalDateTime

import com.dsouzam.githubapi._
import com.dsouzam.language_analysis.tables._
import de.tudarmstadt.ukp.jwktl.api.PartOfSpeech
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.Duration


class Extractor(val client: APIClient, val database: Database) {
  val RANGE = 3
  val NUM_KEYWORDS = 10 // number of keywords to take and store
  val ranker = new TextRank(RANGE, Set(PartOfSpeech.NOUN))
  val repositoriesTable = TableQuery[Repositories]
  val languagesTable = TableQuery[Languages]
  val repositoryLanguagesTable = TableQuery[RepositoryLanguages]

  // queries GitHub and updates database with results
  def update(language: String, num: Int, fromScratch: Boolean = false) = {
    val desiredCount = num.toInt
    val currentCount = if (fromScratch) 0 else getOffset(language)
    if (currentCount < desiredCount) {
      println(s"$currentCount repositories found for $language. Fetching ${desiredCount-currentCount} more.")

      val repos = extract(language, desiredCount, currentCount)

      val languages = repos.flatMap(_.languages.keys)
      val languageInsert = languageInsertAction(languages) // update Languages table
      val repositoryInsert = repositoryInsertAction(repos) // update Repositories table

      Await.result(languageInsert.zip(repositoryInsert), Duration.Inf) // complete first 2 transactions

      // once new languages have been committed to the Languages table, we can update the RepositoryLanguages table
      val repositoryLanguageInsert = repositoryLanguageInsertAction(repos, language)

      Await.result(repositoryLanguageInsert, Duration.Inf)

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
    ranker.rank(str).take(NUM_KEYWORDS).map(_._1).mkString(",")
  }

  def repositoryInsertAction(repositories: Seq[Repository]) = {
    database.run(DBIO.sequence(repositories.map{ r =>
      repositoriesTable.insertOrUpdate((r.url, r.name, r.id, r.description, r.readMe, toTs(r.createdAt),
      toTs(r.updatedAt), toTs(r.pushedAt), r.stars, r.watchers, r.hasPages, r.forks, r.defaultBranch, now))
    }))
  }

  def languageInsertAction(languages: Seq[String]) = {
    database.run(DBIO.sequence(languages.map{ lang =>
      languagesTable.insertOrUpdate((0, lang))
    }))
  }

  def repositoryLanguageInsertAction(repositories: Seq[Repository], primaryLanguage: String) = {
    val languages = getLanguagesMap
    database.run(DBIO.sequence(repositories.flatMap{ r =>
      r.languages.map ( pair =>
        repositoryLanguagesTable.insertOrUpdate((r.id, languages(pair._1), pair._2, pair._1 == primaryLanguage, now))
      )
    }))

  }

  // Returns a map from language name (string) to language id (int)
  def getLanguagesMap = {
    val query = languagesTable.map(lang => (lang.name, lang.id))
    val result = Await.result(database.run(query.result), Duration.Inf)
    result.toMap
  }

  // counts how many records exist for a given language
  def getOffset(language: String): Int = {
    0
//    // TODO switch to languages mapping table
//    val query = repositories.filter(_.language === language).length.result
//    Await.result(database.run(query), Duration.Inf)
  }
}
