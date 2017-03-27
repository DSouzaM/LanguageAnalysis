package com.dsouzam.language_analysis

import com.dsouzam.githubapi._
import de.tudarmstadt.ukp.jwktl.JWKTL
import java.io.File
import java.sql.Timestamp

import de.tudarmstadt.ukp.jwktl.api.PartOfSpeech
import slick.jdbc.MySQLProfile.api._
import slick.jdbc.meta.MTable

import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits._



object Test {
  def main(args: Array[String]): Unit = {
    val client = new APIClient
    println("I'm gonna do a thing")
    for (i <- 1 to 20) {
      println(i)
      client.getRepo("dsouzam","github-api-wrapper")
    }
  }
  def rankApiResult() = {
    val token = APIClient.getToken
    val client = new APIClient(token)
    val repo = client.searchRepos(new RepositorySearchQueryBuilder("").language("C").perPage(1).build, withReadMe = true).head
    val ranker = new TextRank(3, Set(PartOfSpeech.NOUN))
    val results = ranker.rank(repo.description)
    results.foreach(println)
  }
}
