name := "derevo"

version := "0.1"

scalaVersion := "2.12.6"


libraryDependencies in ThisBuild += scalaOrganization.value % "scala-reflect" % scalaVersion.value % Provided

libraryDependencies in ThisBuild += compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.patch)

scalacOptions in ThisBuild ++= Vector(
  "-language:experimental.macros"
)

lazy val derevo: Project = project in file(".")

lazy val cats = project dependsOn derevo
lazy val circe = project dependsOn derevo
lazy val tethys = project dependsOn derevo
