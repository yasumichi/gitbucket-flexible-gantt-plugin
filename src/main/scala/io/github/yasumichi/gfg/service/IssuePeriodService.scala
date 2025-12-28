package io.github.yasumichi.gfg.service

import io.github.yasumichi.gfg.model.IssuePeriod
import io.github.yasumichi.gfg.model.Profile._
import io.github.yasumichi.gfg.model.Profile.profile.blockingApi._
import java.util.Date
import java.util.Calendar
import gitbucket.core.issues.html.issue
import scala.concurrent.ExecutionContext.Implicits.global

import scala.jdk.CollectionConverters._
import gitbucket.core.service.AccountService
import gitbucket.core.service.RepositoryService
import gitbucket.core.service.LabelsService
import gitbucket.core.service.PrioritiesService
import gitbucket.core.service.MilestonesService
import gitbucket.core.model.IssueComponent
import gitbucket.core.service.ActivityService
import gitbucket.core.service.CommitsService
import gitbucket.core.service.IssueCreationService
import gitbucket.core.service.IssuesService
import gitbucket.core.service.MergeService
import gitbucket.core.service.PullRequestService
import gitbucket.core.util.ReadableUsersAuthenticator
import gitbucket.core.util.ReferrerAuthenticator
import gitbucket.core.service.RequestCache
import gitbucket.core.service.WebHookIssueCommentService
import gitbucket.core.service.WebHookPullRequestReviewCommentService
import gitbucket.core.service.WebHookPullRequestService
import gitbucket.core.service.WebHookService
import gitbucket.core.util.WritableUsersAuthenticator
import gitbucket.core.model.CoreProfile

trait IssuePeriodService {
  self: CoreProfile
    with AccountService
    with IssueComponent
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
  import gitbucket.core.service.IssuesService._
  import self.profile.api

  def getIssuePeriod(
      userName: String,
      repositoryName: String,
      issueId: Int
  )(implicit session: Session): List[IssuePeriod] = {
    IssuePeriods
      .filter(i => i.userName === userName && i.repositoryName === repositoryName && i.issueId === issueId)
      .list
  }

  def getIssuePeriods(
      userName: String,
      repositoryName: String
  )(implicit session: Session): List[(io.github.yasumichi.gfg.model.IssuePeriod, gitbucket.core.model.Issue)] = {
    IssuePeriods
      .filter(i => i.userName === userName && i.repositoryName === repositoryName)
      .join(Issues)
      .on { case (t1: IssuePeriods, t2: Issues) =>
        t1.userName === t2.userName && t1.repositoryName === t2.repositoryName && t1.issueId === t2.issueId
      }
      .list
  }

  def getIssuePeriodsByMilestone(
      userName: String,
      repositoryName: String,
      milestoneId: Int
  )(implicit session: Session): List[(gitbucket.core.model.Issue, io.github.yasumichi.gfg.model.IssuePeriod)] = {
    Issues
      .filter(_.byMilestone(userName, repositoryName, milestoneId))
      .join(IssuePeriods)
      .on { case (t1: Issues, t2: IssuePeriods) =>
        t1.userName === t2.userName && t1.repositoryName === t2.repositoryName && t1.issueId === t2.issueId
      }
      .list
  }

  def getIssuePeriodsByLabel(
      userName: String,
      repositoryName: String,
      labelId: Int
  )(implicit session: Session): List[(gitbucket.core.model.Issue, io.github.yasumichi.gfg.model.IssuePeriod)] = {
    val issueIds: Query[Rep[Int], Int, Seq] = IssueLabels.filter(_.byLabel(userName, repositoryName, labelId)).map(_.issueId)
    Issues
      .filter(_.issueId in issueIds)
      .join(IssuePeriods)
      .on { case (t1: Issues, t2: IssuePeriods) =>
        t1.userName === t2.userName && t1.repositoryName === t2.repositoryName && t1.issueId === t2.issueId
      }
      .list
  }

  def insertIssuePeriod(
      userName: String,
      repositoryName: String,
      issueId: Int,
      startDate: Date,
      endDate: Date,
      progress: Int,
      dependencies: String
  )(implicit session: Session): Int = {
    val calendar = Calendar.getInstance()
    calendar.setTime(endDate)
    calendar.add(Calendar.DATE, 1)
    val endDatePlus = calendar.getTime()

    if (getIssuePeriod(userName, repositoryName, issueId).isEmpty) {
      IssuePeriods.insert(
        IssuePeriod(
          userName = userName,
          repositoryName = repositoryName,
          issueId = issueId,
          startDate = startDate,
          endDate = endDatePlus,
          progress = progress,
          dependencies = dependencies
        )
      )
    } else {
      IssuePeriods
        .filter(i => i.userName === userName && i.repositoryName === repositoryName && i.issueId === issueId)
        .map(t => (t.startDate, t.endDate, t.progress, t.dependencies))
        .update((startDate, endDatePlus, progress, dependencies))
    }
  }
}
