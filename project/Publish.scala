import sbt.settingKey

object Publish {
  val publishToGitHub = settingKey[Boolean]("Should artifact be published to GitHub packages or SonarType")
}
