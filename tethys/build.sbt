moduleName := "derevo-tethys"

val tethysVersion = "0.9.0"

libraryDependencies += "com.tethys-json" %% "tethys-core" % tethysVersion
libraryDependencies += "com.tethys-json" %% "tethys-macro-derivation" % tethysVersion
libraryDependencies += "com.tethys-json" %% "tethys-jackson-backend" % tethysVersion % "test"