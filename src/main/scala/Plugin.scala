import gitbucket.core.controller.Context
import gitbucket.core.plugin.Link
import gitbucket.core.service.RepositoryService
import gitbucket.core.service.RepositoryService.RepositoryInfo
import io.github.gitbucket.solidbase.migration.LiquibaseMigration
import io.github.gitbucket.solidbase.model.Version
import io.github.yasumichi.gfg.controller.FlexibleGanttController
import gitbucket.core.model.Issue
import play.twirl.api.Html
import flexiblegantt.html
import gitbucket.core.service.AccountService
import gitbucket.core.settings.html.options
import gitbucket.core.model.Account
import gitbucket.core.model.{Session => _}
import gitbucket.core.model.Session
import gitbucket.core.servlet.Database
import scala.collection.mutable
import gitbucket.core.plugin.PluginRegistry
import gitbucket.core.service.SystemSettingsService
import javax.servlet.ServletContext

class Plugin extends gitbucket.core.plugin.Plugin with AccountService with RepositoryService {
  override val pluginId: String = "flexible-gantt"
  override val pluginName: String = "Flexible Gantt Plugin"
  override val description: String = "Flexible Gantt Plugin"
  override val versions: List[Version] = List(
    new Version("0.1.0", new LiquibaseMigration("update/gitbucket-flexible-gantt-0.1.0.xml")),
    new Version("0.1.1"),
    new Version("0.2.0"),
    new Version("0.3.0")
  )

  override val controllers = Seq(
    "/*" -> new FlexibleGanttController()
  )

  override val assetsMappings = Seq("/flexible-gantt" -> "/flexible-gantt/assets")

  override def javaScripts(
      registry: PluginRegistry,
      context: ServletContext,
      settings: SystemSettingsService.SystemSettings
  ): Seq[(String, String)] = {

    val path = settings.baseUrl.getOrElse(context.getContextPath)

    Seq(
      ".*/flexible-gantt.*" ->
        s"""|</script>
          |
          |<link rel="stylesheet" href="$path/plugin-assets/flexible-gantt/frappe-gantt.css">
          |<script type="text/javascript" src="$path/plugin-assets/flexible-gantt/frappe-gantt.umd.js"></script>
          |
          |<script>
          |""".stripMargin
    )
  }

  override val repositoryMenus = Seq((repositoryInfo: RepositoryInfo, context: Context) =>
    Some(Link("flexible-gantt", "Flexible Gantt", "/flexible-gantt", Some("dashboard")))
  )

  override val systemSettingMenus: Seq[(Context) => Option[Link]] =
    Seq((ctx: Context) => Some(Link("Flexible Gantt", "Flexible Gantt", "admin/flexible-gantt")))

  override val issueSidebars: Seq[(Issue, RepositoryInfo, Context) => Option[Html]] =
    Seq((issue: Issue, repository: RepositoryInfo, context: Context) => {
      implicit val session: Session = Database.getSession(context.request)
      var isEditable = false
      if (!issue.isPullRequest) {
        if (context.loginAccount.isDefined) {
          isEditable = hasDeveloperRole(repository.owner, repository.name, context.loginAccount)
        }
        Some(html.issuesidebar(repository.owner, repository.name, issue.issueId, isEditable)(context))
      } else None
    })
}
