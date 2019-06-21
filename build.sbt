name := "derevo"

version := "0.8.1"

scalaVersion in ThisBuild := "2.12.8"

crossScalaVersions in ThisBuild := List("2.11.12", "2.12.8")

libraryDependencies in ThisBuild += scalaOrganization.value % "scala-reflect" % scalaVersion.value % Provided

libraryDependencies in ThisBuild ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 11 | 12)) => Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch))
    case _                  => Seq()
  }
}

libraryDependencies in ThisBuild += compilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")

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

val compile213 = crossScalaVersions += "2.13.0"

lazy val core = project settings compile213

lazy val cats          = project dependsOn core
lazy val circe         = project dependsOn core settings compile213
lazy val ciris         = project dependsOn core
lazy val tethys        = project dependsOn core
lazy val tschema       = project dependsOn core
lazy val reactivemongo = project dependsOn core
lazy val catsTagless   = project dependsOn core
lazy val pureconfig    = project dependsOn core settings compile213


lazy val derevo = project in file(".") aggregate (
  core, cats, circe, ciris, tethys, tschema, reactivemongo, catsTagless, pureconfig
)
