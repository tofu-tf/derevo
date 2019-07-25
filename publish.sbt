organization in ThisBuild := "org.manatki"

val publishVersion = "0.8.1"

publishMavenStyle in ThisBuild := true

homepage in ThisBuild := Some(url("https://manatki.org/docs/derevo"))

scmInfo in ThisBuild := Some(
  ScmInfo(
    url("https://github.com/manatki/derevo"),
    "git@github.com:manatki/derevo.git"
  ))

import com.typesafe.sbt.SbtGit.git

publishTo in ThisBuild := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)
developers in ThisBuild := List(
  Developer(
    "odomontois",
    "Oleg Nizhnik",
    "odomontois@gmail.com",
    url("https://github.com/odomontois")
  ))

credentials in ThisBuild += Credentials(Path.userHome / ".sbt" / "odo.credentials")
pgpSecretRing := Path.userHome / ".gnupg"/ "secring.gpg"

version in ThisBuild := {
  val branch = git.gitCurrentBranch.value
  if (branch == "master") publishVersion
  else s"$publishVersion-$branch-SNAPSHOT"
}

licenses in ThisBuild += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

sources in (Compile, doc) := Seq.empty
