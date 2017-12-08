import $file.ProjectUtil

import ammonite.ops._
import ProjectUtil._

def indexRustProjects(baseDir: Path): Unit = {
  require(baseDir.isDir)

  val projects = ls.iter(baseDir)
    .filter(_.isDir)
    .map(_.name)
    .map(Project)
    .toVector
    .sortBy(_.name)

  write.over(baseDir/"index.md", projectsPage(projects))
  projects foreach { indexProject(baseDir, _) }
}

private def indexProject(baseDir: Path, project: Project): Unit = {
  val projectDir = baseDir/project.name
  require(projectDir.isDir)

  val projectVersions = ls(projectDir).iterator
    .filter(_.isDir)
    .map(_.name)
    .filterNot(_ == "latest")
    .map(parseProjectVersion)
    .toVector
    .sortBy(_.version)

  val latestVersion = projectVersions.maxBy(_.version).exact
  write.over(projectDir/"index.md",
    projectVersionPage(project, projectVersions, latestVersion))
  write.over(projectDir/'latest/"index.html", projectLatestRedirect(project, latestVersion))
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
