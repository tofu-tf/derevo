import Publish._

name := "derevo"

val publishVersion = "0.11.0"

val common = List(
  scalaVersion := "2.13.1",
  crossScalaVersions := List("2.12.10", "2.13.1"),
  libraryDependencies += scalaOrganization.value % "scala-reflect" % scalaVersion.value % Provided,
  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 11 | 12)) => List(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch))
      case _                  => List()
    }
  },
  libraryDependencies += compilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
  scalacOptions ++= Seq(
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
      case _                       => Nil
    }
  },
  organization := "org.manatki",
  homepage := Some(url("https://manatki.org/docs/derevo")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/manatki/derevo"),
      "git@github.com:manatki/derevo.git"
    )
  ),
  developers := List(
    Developer(
      "odomontois",
      "Oleg Nizhnik",
      "odomontois@gmail.com",
      url("https://github.com/odomontois")
    )
  ),
  licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),

  publishToGitHub := sys.env.contains("GITHUB_PUBLISH"),
  publishTo := {
    if (publishToGitHub.value) Some("GitHub Package Registry" at "https://maven.pkg.github.com/manatki/derevo")
    else if (isSnapshot.value) Some(Opts.resolver.sonatypeSnapshots)
    else sonatypePublishToBundle.value
  },
  publishMavenStyle := true,
  version := {
    if (publishToGitHub.value) s"$publishVersion-${git.gitHeadCommit.value.get}-SNAPSHOT"
    else git.gitCurrentBranch.value match {
      case "master" => publishVersion
      case branch   => s"$publishVersion-$branch-SNAPSHOT"
    }
  },
  credentials ++= {
    if (publishToGitHub.value)
      for {
        user  <- sys.env.get("GITHUB_USER")
        token <- sys.env.get("GITHUB_TOKEN")
      } yield Credentials("GitHub Package Registry", "maven.pkg.github.com", user, token)
    else Some(Path.userHome / ".sbt" / "odo.credentials")
      .filter(_.exists())
      .map(Credentials.apply)
  },
  pgpSecretRing := Path.userHome / ".gnupg" / "secring.gpg"
)

val compile211 = crossScalaVersions += "2.11.12"

lazy val core = project settings common settings compile211

lazy val cats          = project dependsOn core settings common
lazy val circe         = project dependsOn core settings common
lazy val ciris         = project dependsOn core settings common settings (scalacOptions -= "-Xfatal-warnings")
lazy val tethys        = project dependsOn core settings common settings compile211
lazy val tschema       = project dependsOn core settings common
lazy val reactivemongo = project dependsOn core settings common settings compile211
lazy val catsTagless   = project dependsOn core settings common settings compile211
lazy val pureconfig    = project dependsOn core settings common settings compile211
lazy val scalacheck    = project dependsOn core settings common

lazy val derevo = project in file(".") settings (common, skip in publish := true) aggregate (
  core, cats, circe, ciris, tethys, tschema, reactivemongo, catsTagless, pureconfig, scalacheck
)
