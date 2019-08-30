name := "derevo"

scalaVersion in ThisBuild := "2.12.8"

val common = List(
  crossScalaVersions := List("2.12.8"),
  libraryDependencies += scalaOrganization.value % "scala-reflect" % scalaVersion.value % Provided,
  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 11 | 12)) => List(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch))
      case _                  => List()
    }
  },
  libraryDependencies += compilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
  scalacOptions ++= Vector(
    "-deprecation",
    "-feature",
    "-language:experimental.macros",
    "-language:higherKinds",
    "-Xfatal-warnings"
  ),
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, y)) if y == 11 => Seq("-Xexperimental")
      case Some((2, y)) if y == 13 => Seq("-Ymacro-annotations")
      case _                       => Seq.empty[String]
    }
  }
)

val compile213 = crossScalaVersions += "2.13.0"
val compile211 = crossScalaVersions += "2.11.12"

lazy val core = project settings common settings (compile211, compile213)

lazy val cats          = project dependsOn core settings common settings compile213
lazy val circe         = project dependsOn core settings common settings (compile211, compile213)
lazy val ciris         = project dependsOn core settings common settings compile213
lazy val tethys        = project dependsOn core settings common settings (compile211, compile213)
lazy val tschema       = project dependsOn core settings common settings (compile211, compile213)
lazy val reactivemongo = project dependsOn core settings common settings (compile211, compile213)
lazy val catsTagless   = project dependsOn core settings common settings (compile211, compile213)
lazy val pureconfig    = project dependsOn core settings common settings (compile211, compile213)

lazy val derevo = project in file(".") aggregate (
  core, cats, circe, ciris, tethys, tschema, reactivemongo, catsTagless, pureconfig
)
