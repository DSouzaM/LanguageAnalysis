package com.dsouzam.language_analysis.tables

import slick.jdbc.MySQLProfile.api._

class Languages(tag: Tag) extends Table[(Int, String)](tag, Some("main"), "LANGUAGES") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def name = column[String]("NAME", O.Unique, O.SqlType("varchar(50)"))
  def * = (id, name)
}
