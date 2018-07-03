organization in ThisBuild := "ru.tinkoff"

val publishVersion = "0.1"

publishMavenStyle in ThisBuild := true

crossScalaVersions in ThisBuild := Seq("2.11.12", "2.12.6")

import com.typesafe.sbt.SbtGit.git

publishTo in ThisBuild := {
  val nexus = "http://nexus.tcsbank.ru/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/tcs-snapshot")
  else
    Some("releases" at nexus + "content/repositories/tcs")
}

credentials in ThisBuild += Credentials(Path.userHome / ".ivy2" / ".credentials")

version in ThisBuild := {
  val branch = git.gitCurrentBranch.value
  if (List("master", "tinkoff-master") contains branch) publishVersion
  else s"$publishVersion-$branch-SNAPSHOT"
}

updateOptions in ThisBuild := updateOptions.value.withGigahorse(false)

sources in (Compile, doc) := Seq.empty
