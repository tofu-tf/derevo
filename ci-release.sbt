ThisBuild / scalaVersion := Dependencies.Version.scala213
ThisBuild / crossScalaVersions := Seq(
  Dependencies.Version.scala213,
  Dependencies.Version.scala212
)
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  RefPredicate.Equals(Ref.Branch("master")),
  RefPredicate.StartsWith(Ref.Tag("v"))
)

ThisBuild / githubWorkflowJavaVersions := Seq("adopt@1.8", "adopt@1.11")

ThisBuild / githubWorkflowBuildPreamble += WorkflowStep.Sbt(
  List("scalafmtCheckAll", "scalafmtSbtCheck"),
  name = Some("Check formatting")
)

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    name = Some("Publish artifacts"),
    env = Map(
      "PGP_PASSPHRASE"    -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET"        -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)

ThisBuild / versionScheme := Some("early-semver")

ThisBuild / licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / developers := List(
  Developer(
    "Odomontois",
    "Oleg Nizhnik",
    "odomontois@gmail.com",
    url("https://github.com/Odomontois")
  )
)
ThisBuild / homepage := Some(url("https://manatki.org/docs/derevo"))
ThisBuild / scmInfo := Some(
  ScmInfo(url("https://github.com/tofu-tf/derevo"), "scm:git:git@github.com:tofu-tf/derevo.git")
)
ThisBuild / organization := "tf.tofu"
ThisBuild / organizationName := "Tofu"
