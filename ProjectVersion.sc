import $ivy.`com.nthportal::versions:2.0.1`

import com.nthportal.versions.v3.Version

case class ProjectVersion(exact: String, version: Version)

def parseProjectVersion(v: String): ProjectVersion = ProjectVersion(v, Version.parseVersion(v))
