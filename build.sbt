name := "derevo"

version := "0.1"

scalaVersion := "2.12.6"


libraryDependencies += scalaOrganization.value % "scala-reflect" % scalaVersion.value % Provided

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.patch)

scalacOptions ++= Vector(
  "-language:experimental.macros",
  "-Xlog-free-terms"
)

lazy val derevo = project in file(".")

lazy val cats = project dependsOn derevo
