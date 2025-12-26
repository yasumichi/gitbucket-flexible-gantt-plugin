package io.github.yasumichi.gfg.model

trait IssuePeriodComponent { self: gitbucket.core.model.Profile =>
  import profile.api._
  import self._

  lazy val IssuePeriods = TableQuery[IssuePeriods]

  class IssuePeriods(tag: Tag) extends Table[IssuePeriod](tag, "ISSUE_PERIOD") {
    val userName = column[String]("USER_NAME")
    val repositoryName = column[String]("REPOSITORY_NAME")
    val issueId = column[Int]("ISSUE_ID")
    val startDate = column[java.util.Date]("START_DATE")
    val endDate = column[java.util.Date]("END_DATE")
    val progress = column[Int]("PROGRESS")
    val dependencies = column[String]("DEPENDENCIES")
    def * = (
      userName,
      repositoryName,
      issueId,
      startDate,
      endDate,
      progress,
      dependencies
    ).mapTo[IssuePeriod]
  }
}

case class IssuePeriod(
    userName: String,
    repositoryName: String,
    issueId: Int,
    startDate: java.util.Date,
    endDate: java.util.Date,
    progress: Int,
    dependencies: String
)
