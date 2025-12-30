// use twirl template
import flexiblegantt.html

// gitbucket core modules
import gitbucket.core.controller.Context
import gitbucket.core.model.Account
import gitbucket.core.model.Issue
import gitbucket.core.model.Session
import gitbucket.core.model.{Session => _}
import gitbucket.core.plugin.Link
import gitbucket.core.plugin.PluginRegistry
import gitbucket.core.service.AccountService
import gitbucket.core.service.RepositoryService
import gitbucket.core.service.RepositoryService.RepositoryInfo
import gitbucket.core.service.SystemSettingsService
import gitbucket.core.servlet.Database
import gitbucket.core.settings.html.options

// use solidbase
import io.github.gitbucket.solidbase.migration.LiquibaseMigration
import io.github.gitbucket.solidbase.model.Version

// reffer FlexibleGanttController
import io.github.yasumichi.gfg.controller.FlexibleGanttController

// other utilities
import javax.servlet.ServletContext
import play.twirl.api.Html
import scala.collection.mutable

/**
  * Flexible Gantt Plugin
  */
class Plugin extends gitbucket.core.plugin.Plugin with AccountService with RepositoryService {
  override val pluginId: String = "flexible-gantt"
  override val pluginName: String = "Flexible Gantt Plugin"
  override val description: String = "Flexible Gantt Plugin"
  override val versions: List[Version] = List(
    new Version("0.1.0", new LiquibaseMigration("update/gitbucket-flexible-gantt-0.1.0.xml")),
    new Version("0.1.1"),
    new Version("0.2.0"),
    new Version("0.3.0"),
    new Version("0.4.0"),
    new Version("0.4.1"),
    new Version("0.4.2")
  )

  /**
    * add controller
    */
  override val controllers = Seq(
    "/*" -> new FlexibleGanttController()
  )

  /**
    * assset mappings
    */
  override val assetsMappings = Seq("/flexible-gantt" -> "/flexible-gantt/assets")

  /**
    * inject css and JavaScript to Flexible Gantt
    *
    * @param registry refference to PluginRegistry
    * @param context Servlet Context
    * @param settings System Settings
    * @return css and JavaScript
    */
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

  /**
    * add repository menu of Flexible Gantt
    */
  override val repositoryMenus = Seq((repositoryInfo: RepositoryInfo, context: Context) =>
    Some(Link("flexible-gantt", "Flexible Gantt", "/flexible-gantt", Some("dashboard")))
  )

  /**
    * add system settings menu of Flexible Gantt
    */
  override val systemSettingMenus: Seq[(Context) => Option[Link]] =
    Seq((ctx: Context) => Some(Link("Flexible Gantt", "Flexible Gantt", "admin/flexible-gantt")))

  /**
    * add Flexible Gantt sidebar to issues
    */
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
