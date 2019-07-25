moduleName := "derevo-tethys"

libraryDependencies += "com.tethys-json" %% "tethys-core"       % Version.tethys
libraryDependencies += "com.tethys-json" %% "tethys-derivation" % Version.tethys
libraryDependencies += "com.tethys-json" %% "tethys-jackson"    % Version.tethys % Test
libraryDependencies += "org.scalatest"   %% "scalatest"         % Version.scalaTest % Test
