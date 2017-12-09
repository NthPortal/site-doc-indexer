import $file.IndexUtils

import ammonite.ops._
import IndexUtils._

def indexScalaProjects(baseDir: Path): Unit = {
  require(baseDir.isDir)

  val projects = readProjectsFromDir(baseDir)

  write.over(baseDir/"index.md", projectsPage(projects))
  projects foreach { indexProject(baseDir, _) }
}

private def indexProject(baseDir: Path, project: Project): Unit = {
  val projectDir = baseDir/project.name
  require(projectDir.isDir)

  val scalaVersions = ls.iter(projectDir)
    .filter(_.isDir)
    .map(_.name)
    .filter(_.startsWith("scala-"))
    .map(parseScalaVersion)
    .toVector
    .sortBy(_.exact)

  write.over(projectDir/"index.md", scalaVersionPage(project, scalaVersions))
  scalaVersions foreach { indexVersions(projectDir, project, _) }
}

private def indexVersions(projectDir: Path, project: Project, scalaVersion: ScalaVersion): Unit = {
  val versionDir = projectDir/scalaVersion.exact
  require(versionDir.isDir)

  val projectVersions = readVersionsFromDir(versionDir)
  val latestVersion = projectVersions.maxBy(_.version).exact

  write.over(versionDir/"index.md",
    projectVersionPage(project, scalaVersion, projectVersions, latestVersion))
  write.over(versionDir/'latest/"index.html", projectLatestRedirect(latestVersion))
}

private def projectsPage(projects: Seq[Project]): String = {
  s"""# Scala Projects
     |
     |${projects map { p => s"[$p]($p)" } mkString "\n\n"}
     |
     |----------------
     |
     |[<=](..)
     |""".stripMargin
}

private def scalaVersionPage(project: Project, scalaVersions: Seq[ScalaVersion]): String = {
  s"""# $project
     |
     |${scalaVersions map { v => s"[${v.pretty}](${v.exact})" } mkString "\n\n"}
     |
     |----------------
     |
     |[<=](..)
     |""".stripMargin
}

private def projectVersionPage(project: Project,
                               scalaVersion: ScalaVersion,
                               projectVersions: Seq[ProjectVersion],
                               latestVersion: String): String = {
  s"""# $project - ${scalaVersion.pretty}
     |
     |[latest]($latestVersion)
     |
     |${projectVersions map { v => s"[${v.version}](${v.exact})" } mkString "\n\n"}
     |
     |----------------
     |
     |[<=](..)
     |""".stripMargin
}

private def projectLatestRedirect(latestVersion: String): String = {
  s"""<html>
     |  <head>
     |    <meta http-equiv="refresh" content="0; url=../$latestVersion">
     |  </head>
     |</html>
     |""".stripMargin
}

private case class ScalaVersion(exact: String, pretty: String)

private def parseScalaVersion(version: String): ScalaVersion = {
  version split '-' match {
    case Array(_, ver) => ScalaVersion(version, s"Scala $ver")
    case _ => ScalaVersion(version, version)
  }
}
