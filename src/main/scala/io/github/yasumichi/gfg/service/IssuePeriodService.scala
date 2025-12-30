package io.github.yasumichi.gfg.service

// use functions of gitbucket core
import gitbucket.core.issues.html.issue
import gitbucket.core.model.CoreProfile
import gitbucket.core.model.IssueComponent
import gitbucket.core.service.AccountService
import gitbucket.core.service.ActivityService
import gitbucket.core.service.CommitsService
import gitbucket.core.service.IssueCreationService
import gitbucket.core.service.IssuesService
import gitbucket.core.service.LabelsService
import gitbucket.core.service.MergeService
import gitbucket.core.service.MilestonesService
import gitbucket.core.service.PrioritiesService
import gitbucket.core.service.PullRequestService
import gitbucket.core.service.RepositoryService
import gitbucket.core.service.RequestCache
import gitbucket.core.service.WebHookIssueCommentService
import gitbucket.core.service.WebHookPullRequestReviewCommentService
import gitbucket.core.service.WebHookPullRequestService
import gitbucket.core.service.WebHookService
import gitbucket.core.util.ReadableUsersAuthenticator
import gitbucket.core.util.ReferrerAuthenticator
import gitbucket.core.util.WritableUsersAuthenticator

// use self models of Flexible Gantt
import io.github.yasumichi.gfg.model.IssuePeriod
import io.github.yasumichi.gfg.model.Profile._
import io.github.yasumichi.gfg.model.Profile.profile.blockingApi._

// other utilities
import java.util.Calendar
import java.util.Date
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters._

/**
  * trait for database access to ISSUE_PERIOD and relative tables
  */
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

  /**
    * get one issue by issue id
    *
    * @param userName repository owner
    * @param repositoryName repository name
    * @param issueId issue id
    * @param session session of database
    * @return period of target issue
    */
  def getIssuePeriod(
      userName: String,
      repositoryName: String,
      issueId: Int
  )(implicit session: Session): List[IssuePeriod] = {
    IssuePeriods
      .filter(i => i.userName === userName && i.repositoryName === repositoryName && i.issueId === issueId)
      .list
  }

  /**
    * get issues has periods
    *
    * @param userName repository owner
    * @param repositoryName repository name
    * @param session session of database 
    * @return issues has periods
    */
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

  /**
    * get issues has periods filtered by milestone id
    *
    * @param userName repository owner
    * @param repositoryName repository name
    * @param milestoneId milestone id
    * @param session session of database
    * @return issues has periods filtered by milestone id
    */
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

  /**
    * get issues has periods filtered by label name
    *
    * @param userName repository owner
    * @param repositoryName repository name
    * @param labelName label name
    * @param session session of database
    * @return issues has periods filtered by label name 
    */
  def getIssuePeriodsByLabel(
      userName: String,
      repositoryName: String,
      labelName: String
  )(implicit session: Session): List[(gitbucket.core.model.Issue, io.github.yasumichi.gfg.model.IssuePeriod)] = {
    val issueIds: Query[Rep[Int], Int, Seq] = Labels
      .filter(_.byLabel(userName, repositoryName, labelName))
      .join(IssueLabels)
      .on { case (t1: Labels, t2: IssueLabels) =>
        t1.labelId === t2.labelId && t1.userName === t2.userName && t1.repositoryName === t2.repositoryName
      }
      .map { case (t1: Labels, t2: IssueLabels) => t2.issueId }
    Issues
      .filter(_.byRepository(userName, repositoryName))
      .filter(_.issueId in issueIds)
      .join(IssuePeriods)
      .on { case (t1: Issues, t2: IssuePeriods) =>
        t1.userName === t2.userName && t1.repositoryName === t2.repositoryName && t1.issueId === t2.issueId
      }
      .list
  }

  /**
    * insert or update period of one issue to database table ISSUE_PERIOD
    *
    * @param userName repository owner
    * @param repositoryName repository name
    * @param issueId issue id
    * @param startDate start date of issue
    * @param endDate end date of issue
    * @param progress progress of issue
    * @param dependencies dependencies of issue
    * @param session session of database
    * @return count of record
    */
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
