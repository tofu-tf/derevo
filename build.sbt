name := "derevo"

version := "0.1"

scalaVersion := "2.12.6"


libraryDependencies in ThisBuild += scalaOrganization.value % "scala-reflect" % scalaVersion.value % Provided

libraryDependencies in ThisBuild += compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.patch)

scalacOptions in ThisBuild ++= Vector(
  "-language:experimental.macros"
)

lazy val core = project

lazy val cats = project dependsOn core
lazy val circe = project dependsOn core
lazy val tethys = project dependsOn core

lazy val derevo = project in file (".") aggregate (core, cats, circe, tethys)
