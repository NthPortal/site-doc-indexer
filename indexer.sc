import $ivy.`com.nthportal::versions:2.0.1`

import ammonite.ops._
import com.nthportal.versions.v3.Version

@main
def main(path: Path = pwd): Unit = {
  indexScalaProjects(path/'scala)
}

def indexScalaProjects(baseDir: Path): Unit = {
  require(baseDir.isDir)

  val projects = ls.iter(baseDir)
    .filter(_.isDir)
    .map(_.name)
    .toVector
    .sorted

  write.over(baseDir/"index.md", projectsPage(projects))
  projects foreach { indexProject(baseDir, _) }
}

private def indexProject(baseDir: Path, project: String): Unit = {
  val projectDir = baseDir/project
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

private def indexVersions(projectDir: Path, project: String, scalaVersion: ScalaVersion): Unit = {
  val versionDir = projectDir/scalaVersion.exact
  require(versionDir.isDir)

  val projectVersions = ls(versionDir).iterator
    .filter(_.isDir)
    .map(_.name)
    .filterNot(_ == "latest")
    .map(parseProjectVersion)
    .toVector
    .sortBy(_.version)

  val latestVersion = projectVersions.maxBy(_.version).exact
  write.over(versionDir/"index.md",
    projectVersionPage(project, scalaVersion, projectVersions, latestVersion))
  write.over(versionDir/'latest/"index.html", projectLatestRedirect(latestVersion))
}

private def projectsPage(projects: Seq[String]): String = {
  s"""# Scala Projects
     |
     |${projects map { s => s"[$s]($s)" } mkString "\n\n"}
     |
     |----------------
     |
     |[<=](..)
     |""".stripMargin
}

private def parseScalaVersion(version: String): ScalaVersion = {
  version split '-' match {
    case Array(_, ver) => ScalaVersion(version, s"Scala $ver")
    case _ => ScalaVersion(version, version)
  }
}

private def scalaVersionPage(project: String, scalaVersions: Seq[ScalaVersion]): String = {
  s"""# $project
     |
     |${scalaVersions map { v => s"[${v.pretty}](${v.exact})" } mkString "\n\n"}
     |
     |----------------
     |
     |[<=](..)
     |""".stripMargin
}

private def parseProjectVersion(v: String): ProjectVersion = ProjectVersion(v, Version.parseVersion(v))

private def projectVersionPage(project: String,
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

private case class ProjectVersion(exact: String, version: Version)
