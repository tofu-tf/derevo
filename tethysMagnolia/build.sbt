moduleName := "derevo-tethys-magnolia"

libraryDependencies += "com.tethys-json" %% "tethys-core"    % Version.tethys
libraryDependencies += "com.propensive"  %% "magnolia"       % Version.magnolia
libraryDependencies += "com.tethys-json" %% "tethys-jackson" % Version.tethys % Test
libraryDependencies += "org.scalatest"   %% "scalatest"      % Version.scalaTest % Test
