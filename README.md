# derevo
Multiple instance derivations inside a single macro annotation

| [![Build Status](https://travis-ci.com/manatki/derevo.svg?branch=master)](https://travis-ci.com/Tmanatki/derevo) | [![Maven Central](https://img.shields.io/maven-central/v/org.manatki/derevo_2.12.svg)](https://search.maven.org/search?q=org.manatki.derevo) | 

```scala
val version = "0.10.1"

"org.manatki" %% "derevo-core"         % version  
"org.manatki" %% "derevo-cats"         % version  
"org.manatki" %% "derevo-circe"        % version  
"org.manatki" %% "derevo-ciris"        % version  
"org.manatki" %% "derevo-tethys"       % version  
"org.manatki" %% "derevo-tschema"      % version  
"org.manatki" %% "derevo-rmongo"       % version  
"org.manatki" %% "derevo-cats-tagless" % version  
"org.manatki" %% "derevo-pureconfig"   % version
```

Requires ["paradise"](https://github.com/scalamacros/paradise) for scala older than "2.13" and "-Ymacro-annotations" scalac option for scala "2.13".
