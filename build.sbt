name := "derevo"

version := "0.5.0"

crossScalaVersions in ThisBuild := List("2.11.12", "2.12.6")

libraryDependencies in ThisBuild += scalaOrganization.value % "scala-reflect" % scalaVersion.value % Provided

libraryDependencies in ThisBuild += compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch)

scalacOptions in ThisBuild ++= Vector(
  "-deprecation",
  "-feature",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-Xfatal-warnings"
)

scalacOptions in ThisBuild ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, y)) if y == 11 => Seq("-Xexperimental")
    case _                       => Seq.empty[String]
  }
}

lazy val core = project

lazy val cats          = project dependsOn core
lazy val circe         = project dependsOn core
lazy val tethys        = project dependsOn core
lazy val reactivemongo = project dependsOn core

lazy val derevo = project in file(".") aggregate (core, cats, circe, tethys, reactivemongo)
