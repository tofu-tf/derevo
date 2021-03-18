name := "derevo"
import com.typesafe.sbt.SbtGit.git

val publishVersion = "0.12.1"

val common = List(
  scalaVersion := "2.13.4",
  crossScalaVersions := List("2.12.13", "2.13.4"),
  libraryDependencies += scalaOrganization.value % "scala-reflect" % scalaVersion.value  % Provided,
  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 11 | 12)) => List(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch))
      case _                  => List()
    }
  },
  libraryDependencies += compilerPlugin("org.typelevel" %% "kind-projector" % "0.11.3" cross CrossVersion.patch),
  scalacOptions ++= Vector(
    "-deprecation",
    "-feature",
    "-language:experimental.macros",
    "-language:higherKinds",
    "-Xfatal-warnings",
  ),
  Test / scalacOptions ++= Vector(
    "-language:implicitConversions",
  ),
  libraryDependencies += "io.estatico"          %% "newtype"       % Version.estatico    % Test,
  libraryDependencies += "org.rudogma"          %% "supertagged"   % Version.supertagged % Test,
  libraryDependencies += "org.scalameta"        %% "munit"         % Version.munit       % "test",
  libraryDependencies += "org.scalatest"        %% "scalatest"     % Version.scalaTest   % "test",
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, y)) if y == 13 => Seq("-Ymacro-annotations")
      case _                       => Seq.empty[String]
    }
  },
  publishMavenStyle := true,
  homepage := Some(url("https://manatki.org/docs/derevo")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/tofu-tf/derevo"),
      "git@github.com:tofu-tf/derevo.git"
    )
  ),
  publishTo := {
    if (isSnapshot.value) {
      Some(Opts.resolver.sonatypeSnapshots)
    } else sonatypePublishToBundle.value
  },
  developers := List(
    Developer(
      "odomontois",
      "Oleg Nizhnik",
      "odomontois@gmail.com",
      url("https://github.com/odomontois")
    )
  ),
  credentials ++= ((Path.userHome / ".sbt" / "odo.credentials") :: Nil)
    .filter(_.exists())
    .map(Credentials.apply),
  pgpSecretRing := Path.userHome / ".gnupg" / "secring.gpg",
  organization := "tf.tofu",
  version := {
    val branch = git.gitCurrentBranch.value
    if (branch == "master") publishVersion
    else s"$publishVersion-$branch-SNAPSHOT"
  },
  licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
)

lazy val core           = project settings common
lazy val cats           = project dependsOn core settings common
lazy val circe          = project dependsOn core settings common
lazy val circeMagnolia  = project dependsOn core settings common
lazy val ciris          = project dependsOn core settings common settings (scalacOptions -= "-Xfatal-warnings")
lazy val tethys         = project dependsOn core settings common
lazy val tethysMagnolia = project dependsOn core settings common
lazy val tschema        = project dependsOn core settings common
lazy val reactivemongo  = project dependsOn core settings common
lazy val catsTagless    = project dependsOn core settings common
lazy val pureconfig     = project dependsOn core settings common
lazy val scalacheck     = project dependsOn core settings common
lazy val tests          =
  project
    .dependsOn(core, circe, ciris, tethys, reactivemongo, catsTagless, pureconfig)
    .settings(common, skip in publish := true)

lazy val derevo = project
  .in(file("."))
  .settings(common, skip in publish := true)
  .aggregate(
    core,
    cats,
    circe,
    circeMagnolia,
    ciris,
    tethys,
    tschema,
    reactivemongo,
    catsTagless,
    pureconfig,
    scalacheck,
    tests,
  )

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("checkfmt", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
