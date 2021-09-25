lazy val commonSettings    = Seq(
  libraryDependencies ++= {
    (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 12)) => Seq(compilerPlugin(Dependencies.kindProjector), compilerPlugin(Dependencies.macroParadise))
      case Some((2, 13)) => Seq(compilerPlugin(Dependencies.kindProjector))
      case _             => Seq.empty
    }) ++
      Seq(Dependencies.scalaReflect(scalaOrganization.value, scalaVersion.value) % Provided)
  },
  scalacOptions := {
    val opts     = scalacOptions.value
    val excluded = Set("-Xfatal-warnings")
    val wconf    = "-Wconf:" +
      Set("unused-params", "unused-privates", "unused-pat-vars", "other-match-analysis")
        .foldRight("any:wv")((s, acc) => s"cat=$s:s,$acc")
    val mannot   = "-Ymacro-annotations"
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 12)) => opts.filterNot(excluded) ++ Seq(wconf)
      case Some((2, 13)) => opts.filterNot(excluded) ++ Seq(wconf, mannot)
      case _             => opts
    }
  },
  Test / fork   := true,
  libraryDependencies ++= Seq(Dependencies.scalatest, Dependencies.estatico, Dependencies.supertagged).map(_ % Test),
  resolvers += Resolver.sonatypeRepo("releases"),
)

lazy val noPublishSettings =
  commonSettings ++ Seq(publish := {}, publishArtifact := false, publishTo := None, publish / skip := true)

lazy val publishSettings   = commonSettings ++ Seq(
  pomIncludeRepository   := { _ =>
    false
  },
  Test / publishArtifact := false
)

lazy val derevo            = project
  .in(file("."))
  .settings(noPublishSettings)
  .aggregate(
    cats,
    catsTagless,
    circe,
    circeMagnolia,
    ciris,
    core,
    pureconfig,
    reactivemongo,
    scalacheck,
    tethys,
    tethysMagnolia,
    tests,
  )

lazy val core              =
  (project in file("modules/core"))
    .settings(publishSettings)
    .settings(
      name := "derevo-core"
    )

lazy val cats              =
  (project in file("modules/cats"))
    .settings(publishSettings)
    .settings(
      name := "derevo-cats",
      libraryDependencies ++= Seq(Dependencies.magnolia, Dependencies.catsCore),
    )
    .dependsOn(core)

lazy val catsTagless       =
  (project in file("modules/catsTagless"))
    .settings(publishSettings)
    .settings(
      name := "derevo-cats-tagless",
      libraryDependencies ++= Seq(Dependencies.catsTaglessCore, Dependencies.catsTaglessMacros),
    )
    .dependsOn(core)

lazy val circe             =
  (project in file("modules/circe"))
    .settings(publishSettings)
    .settings(
      name := "derevo-circe",
      libraryDependencies ++= Seq(Dependencies.circeCore, Dependencies.circeDerivation),
      libraryDependencies ++= Seq(Dependencies.circeParser).map(_ % Test)
    )
    .dependsOn(core)

lazy val circeMagnolia     =
  (project in file("modules/circeMagnolia"))
    .settings(publishSettings)
    .settings(
      name := "derevo-circe-magnolia",
      libraryDependencies ++= Seq(Dependencies.circeMagnolia),
      libraryDependencies ++= Seq(Dependencies.circeParser).map(_ % Test)
    )
    .dependsOn(core)

lazy val ciris             =
  (project in file("modules/ciris"))
    .settings(publishSettings)
    .settings(
      name := "derevo-ciris",
      libraryDependencies ++= Seq(Dependencies.ciris, Dependencies.typesafeConfig, Dependencies.magnolia),
    )
    .dependsOn(core)

lazy val pureconfig        =
  (project in file("modules/pureconfig"))
    .settings(publishSettings)
    .settings(
      name := "derevo-pureconfig",
      libraryDependencies ++= Seq(Dependencies.pureconfig, Dependencies.pureconfigMagnolia),
    )
    .dependsOn(core)

lazy val reactivemongo     =
  (project in file("modules/reactivemongo"))
    .settings(publishSettings)
    .settings(
      name := "derevo-reactivemongo",
      libraryDependencies ++= Seq(Dependencies.reactivemongoBsonMacros),
    )
    .dependsOn(core)

lazy val tethys            =
  (project in file("modules/tethys"))
    .settings(publishSettings)
    .settings(
      name := "derevo-tethys",
      libraryDependencies ++= Seq(Dependencies.tethysCore, Dependencies.tethysDerivation),
      libraryDependencies ++= Seq(Dependencies.tethysJackson).map(_ % Test)
    )
    .dependsOn(core)

lazy val tethysMagnolia    =
  (project in file("modules/tethysMagnolia"))
    .settings(publishSettings)
    .settings(
      name := "derevo-tethys-magnolia",
      libraryDependencies ++= Seq(Dependencies.tethysCore, Dependencies.magnolia),
      libraryDependencies ++= Seq(Dependencies.tethysJackson).map(_ % Test)
    )
    .dependsOn(core)

lazy val scalacheck        =
  (project in file("modules/scalacheck"))
    .settings(publishSettings)
    .settings(
      name := "derevo-scalacheck",
      libraryDependencies ++= Seq(Dependencies.scalacheck, Dependencies.magnolia),
    )
    .dependsOn(core)

lazy val tests             =
  (project in file("modules/tests"))
    .settings(noPublishSettings)
    .dependsOn(core, circe, ciris, tethys, reactivemongo, catsTagless, pureconfig)
