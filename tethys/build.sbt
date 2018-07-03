moduleName := "derevo-tethys"

val tethysVersion = "0.6.3"

libraryDependencies += "com.tethys-json" %% "tethys-core" % tethysVersion
libraryDependencies += "com.tethys-json" %% "tethys-macro-derivation" % tethysVersion
libraryDependencies += "com.tethys-json" %% "tethys-jackson-backend" % tethysVersion % "test"

scalacOptions in Test += "-Ymacro-debug-lite"
