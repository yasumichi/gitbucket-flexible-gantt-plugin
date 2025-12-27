package io.github.yasumichi.gfg.controller

import flexiblegantt.html
import gitbucket.core.api.ApiError
import gitbucket.core.controller.ControllerBase
import gitbucket.core.service.IssueCreationService
import gitbucket.core.service.RepositoryService.RepositoryInfo
import gitbucket.core.service._
import gitbucket.core.util.ReferrerAuthenticator
import gitbucket.core.util.StringUtil._
import io.github.yasumichi.gfg.service.IssuePeriodService
import io.github.yasumichi.gfg.model.IssuePeriodComponent
import gitbucket.core.model.CoreProfile
import gitbucket.core.util.WritableUsersAuthenticator
import java.util.Date
import java.text.SimpleDateFormat
import gitbucket.core.model.Session
import gitbucket.core.servlet.Database

import org.slf4j.LoggerFactory
import gitbucket.core.util.ReadableUsersAuthenticator
import gitbucket.core.model.IssueComponent

class FlexibleGanttController
    extends FlexibleGanttControllerBase
    with AccountService
    with ActivityService
    with CommitsService
    with CoreProfile
    with IssueComponent
    with IssueCreationService
    with IssuePeriodService
    with IssuesService
    with LabelsService
    with MergeService
    with MilestonesService
    with PrioritiesService
    with PullRequestService
    with ReadableUsersAuthenticator
    with ReferrerAuthenticator
    with RepositoryService
    with RequestCache
    with WebHookIssueCommentService
    with WebHookPullRequestReviewCommentService
    with WebHookPullRequestService
    with WebHookService
    with WritableUsersAuthenticator

trait FlexibleGanttControllerBase extends ControllerBase {

  self: IssuePeriodService
    with IssueComponent
    with AccountService
    with ActivityService
    with CommitsService
    with IssueCreationService
    with IssuesService
    with LabelsService
    with MergeService
    with MilestonesService
    with PrioritiesService
    with PullRequestService
    with ReadableUsersAuthenticator
    with ReferrerAuthenticator
    with RepositoryService
    with RequestCache
    with WebHookIssueCommentService
    with WebHookPullRequestReviewCommentService
    with WebHookPullRequestService
    with WebHookService
    with WritableUsersAuthenticator =>

  private val logger = LoggerFactory.getLogger(classOf[FlexibleGanttController])

  get("/:owner/:repository/flexible-gantt") {
    referrersOnly { repository: RepositoryInfo =>
      {
        implicit val session: Session = Database.getSession(context.request)
        html.flexiblegantt(repository, isIssueManageable(repository))
      }
    }
  }

  ajaxGet("/:owner/:repository/flexible-gantt/issues")(readableUsersOnly { repository =>
    implicit val session: Session = Database.getSession(context.request)
    contentType = formats("json")
    org.json4s.jackson.Serialization.write(
      "list" ->
        getIssuePeriods(repository.owner, repository.name)
          .map { t =>
            Map(
              "id" -> t._2.issueId.toString(),
              "name" -> t._2.title,
              "start" -> t._1.startDate,
              "end" -> t._1.endDate,
              "progress" -> t._1.progress,
              "dependencies" -> t._1.dependencies
            )
          }
    )
  })

  ajaxGet("/:owner/:repository/flexible-gantt/issues/:issueId")(readableUsersOnly { repository =>
    implicit val session: Session = Database.getSession(context.request)
    contentType = formats("json")
    val issueId: Int = params("issueId").toInt
    org.json4s.jackson.Serialization.write(
      "period" ->
        getIssuePeriod(repository.owner, repository.name, issueId)
          .map { t =>
            Map(
              "startDate" -> t.startDate,
              "endDate" -> t.endDate,
              "progress" -> t.progress,
              "dependencies" -> t.dependencies
            )
          }
    )
  })

  ajaxPost("/:owner/:repository/issues/:issueId/period")(writableUsersOnly { repository =>
    context.withLoginAccount { loginAccount =>
      implicit val session: Session = Database.getSession(context.request)
      val formatter: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")

      val userName = params("owner")
      val repositoryName = params("repository")
      val issueId = params("issueId")

      val startDate: Date = if (params("startDate") == "") null else formatter.parse(params("startDate"))
      val endDate: Date = if (params("endDate") == "") null else formatter.parse(params("endDate"))
      val progress = params("progress")
      val dependencies = params("dependencies")

      logger.info("params get")

      logger.info("upsertIssuePeriod call")
      insertIssuePeriod(userName, repositoryName, issueId.toInt, startDate, endDate, progress.toInt, dependencies)

      org.json4s.jackson.Serialization.write(
        Map(
          "message" -> s"updated issue#${issueId} period"
        )
      )
    }
  })
}
