import $file.ScalaIndexer

import ammonite.ops._
import ScalaIndexer._

@main
def main(path: Path = pwd): Unit = {
  indexScalaProjects(path/'scala)
}
