package com.dsouzam.language_analysis.tables

import java.sql.Timestamp
import slick.jdbc.MySQLProfile.api._

class Repositories(tag: Tag) extends Table[(String, String, Int, String, String, String, String, Timestamp, Timestamp, Timestamp, Int, Int, Boolean, Int, String, Timestamp, String)](tag, Some("main"), "REPOSITORIES") {
  // case class mapping
  def url = column[String]("URL")
  def name = column[String]("NAME")
  def id = column[Int]("ID", O.PrimaryKey)
  def description = column[String]("DESCRIPTION")
  def readMe = column[String]("README")
  def language = column[String]("LANGUAGE")
  def otherLanguages = column[String]("OTHER_LANGUAGES")
  def createdAt = column[Timestamp]("CREATED_AT") // convert java.util.LocalDateTime to java.sql.Timestamp
  def updatedAt = column[Timestamp]("UPDATED_AT")
  def pushedAt = column[Timestamp]("PUSHED_AT")
  def stars = column[Int]("STARS")
  def watchers = column[Int]("WATCHERS")
  def hasPages = column[Boolean]("HAS_PAGES")
  def forks = column[Int]("FORKS")
  def defaultBranch = column[String]("DEFAULT_BRANCH")
  // meta columns
  def insertedAt = column[Timestamp]("INSERTED_AT")
  def keywords = column[String]("KEYWORDS")
  def * = (url, name, id, description, readMe, language, otherLanguages, createdAt, updatedAt, pushedAt, stars, watchers, hasPages, forks, defaultBranch, insertedAt, keywords)
}
