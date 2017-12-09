import $file.IndexUtils

import ammonite.ops._
import IndexUtils._

def indexRustProjects(baseDir: Path): Unit = {
  require(baseDir.isDir)

  val projects = readProjectsFromDir(baseDir)

  write.over(baseDir/"index.md", projectsPage(projects))
  projects foreach { indexVersions(baseDir, _) }
}

private def indexVersions(baseDir: Path, project: Project): Unit = {
  val versionDir = baseDir/project.name
  require(versionDir.isDir)

  val projectVersions = readVersionsFromDir(versionDir)
  val latestVersion = projectVersions.maxBy(_.version).exact

  write.over(versionDir/"index.md",
    projectVersionPage(project, projectVersions, latestVersion))
  write.over(versionDir/'latest/"index.html", projectLatestRedirect(project, latestVersion))
}

private def projectsPage(projects: Seq[Project]): String = {
  s"""# Rust Projects
     |
     |${projects map { p => s"[$p]($p)" } mkString "\n\n"}
     |
     |----------------
     |
     |[<=](..)
     |""".stripMargin
}

private def projectVersionPage(project: Project,
                               projectVersions: Seq[ProjectVersion],
                               latestVersion: String): String = {
  val underscoreProject = projectNameWithUnderscores(project)

  s"""# $project
     |
     |[latest]($latestVersion/$underscoreProject)
     |
     |${projectVersions map { v => s"[${v.version}](${v.exact}/$underscoreProject)" } mkString "\n\n"}
     |
     |----------------
     |
     |[<=](..)
     |""".stripMargin
}

private def projectLatestRedirect(project: Project, latestVersion: String): String = {
  s"""<html>
     |  <head>
     |    <meta http-equiv="refresh" content="0; url=../$latestVersion/${projectNameWithUnderscores(project)}">
     |  </head>
     |</html>
     |""".stripMargin
}

private def projectNameWithUnderscores(project: Project): String = project.name.replace('-', '_')
