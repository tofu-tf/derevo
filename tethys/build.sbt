moduleName := "derevo-tethys"

val tethysVersion = "0.9.0"

libraryDependencies += "com.tethys-json" %% "tethys-core"       % tethysVersion
libraryDependencies += "com.tethys-json" %% "tethys-derivation" % tethysVersion
libraryDependencies += "com.tethys-json" %% "tethys-jackson"    % tethysVersion % Test
libraryDependencies += "org.scalatest"   %% "scalatest"         % Version.scalaTest % Test
