moduleName := "derevo-circe"

libraryDependencies += "io.circe" %% "circe-core"       % Version.circe
libraryDependencies += "io.circe" %% "circe-derivation" % Version.circeDerivation
libraryDependencies += "io.circe" %% "circe-parser"     % Version.circe % "test"
