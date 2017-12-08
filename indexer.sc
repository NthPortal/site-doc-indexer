import $file.RustIndexer
import $file.ScalaIndexer

import ammonite.ops._
import RustIndexer.indexRustProjects
import ScalaIndexer.indexScalaProjects

@main
def main(path: Path = pwd): Unit = {
  indexScalaProjects(path/'scala)
  indexRustProjects(path/'rust)
}
