libraryDependencies += "com.propensive" %% "magnolia" % "0.7.1"
libraryDependencies += "org.typelevel" %% "cats-core" % "1.1.0"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.patch)

scalacOptions ++= Vector(
  "-language:experimental.macros",
  "-Xlog-free-terms",
  "-Ymacro-debug-lite"
)