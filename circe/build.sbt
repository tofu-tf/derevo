moduleName := "derevo-circe"

val circeVersion = "0.11.1"


libraryDependencies += "io.circe" %% "circe-core" % circeVersion
libraryDependencies += "io.circe" %% "circe-derivation" % "0.11.0-M1"
libraryDependencies += "io.circe" %% "circe-parser" % circeVersion % "test"