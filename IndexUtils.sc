import $ivy.`com.nthportal::versions:2.0.1`

import ammonite.ops._
import com.nthportal.versions.v3.Version

case class Project(name: String) {
  override def toString: String = name
}

case class ProjectVersion(exact: String, version: Version)

def readProjectsFromDir(dir: Path): Seq[Project] = {
  ls.iter(dir)
    .filter(_.isDir)
    .map(_.name)
    .map(Project)
    .toVector
    .sortBy(_.name)
}

def readVersionsFromDir(dir: Path): Seq[ProjectVersion] = {
  ls(dir).iterator
    .filter(_.isDir)
    .map(_.name)
    .filterNot(_ == "latest")
    .map(v => ProjectVersion(v, Version.parseVersion(v)))
    .toVector
    .sortBy(_.version)
}
