package com.dsouzam.language_analysis.tables

import java.sql.Timestamp

import slick.jdbc.MySQLProfile.api._


class RepositoryLanguages(tag: Tag) extends Table[(Int, Int, Long, Boolean, Timestamp)](tag, Some("main"), "REPOSITORY_LANGUAGES") {
  def repositoryID = column[Int]("REPOSITORY_ID")
  def languageID = column[Int]("LANGUAGE_ID")
  def bytes = column[Long]("BYTES")
  def isPrimary = column[Boolean]("IS_PRIMARY")
  def updatedAt = column[Timestamp]("INSERTED_AT")

  def * = (repositoryID, languageID, bytes, isPrimary, updatedAt)
  def pk = primaryKey("repository_language_pk", (repositoryID, languageID))
}
