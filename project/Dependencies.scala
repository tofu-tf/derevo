import sbt._

object Dependencies {
  object Version {
    val scala212 = "2.12.16"

    val scala213 = "2.13.12"

    val scalatest = "3.2.13"

    val catsTagless = "0.14.0"

    val cats = "2.7.0"

    val circe = "0.14.2"

    val circeMagnolia = "0.7.0"

    val circeDerivation = "0.13.0-M5"

    val pureConfig = "0.17.1"

    val magnolia = "0.17.0"

    val typesafeConfig = "1.4.2"

    val ciris = "1.2.1"

    val reactivemongo = "0.20.13"

    val tethys = "0.26.0"

    val scalacheck = "1.16.0"

    val estatico = "0.4.4"

    val supertagged = "2.0-RC2"

    val kindProjector = "0.13.2"

    val macroParadise = "2.1.1"

    val sangria = "3.2.0"

    val zioJson = "0.6.2"

    val zioSchema = "1.1.1"

    val vulcan = "1.10.1"
  }

  lazy val magnolia                = "com.propensive"        %% "magnolia"                  % Version.magnolia
  lazy val catsCore                = "org.typelevel"         %% "cats-core"                 % Version.cats
  lazy val catsTaglessCore         = "org.typelevel"         %% "cats-tagless-core"         % Version.catsTagless
  lazy val catsTaglessMacros       = "org.typelevel"         %% "cats-tagless-macros"       % Version.catsTagless
  lazy val circeCore               = "io.circe"              %% "circe-core"                % Version.circe
  lazy val circeDerivation         = "io.circe"              %% "circe-derivation"          % Version.circeDerivation
  lazy val circeMagnolia           = "io.circe"              %% "circe-magnolia-derivation" % Version.circeMagnolia
  lazy val typesafeConfig          = "com.typesafe"           % "config"                    % Version.typesafeConfig
  lazy val ciris                   = "is.cir"                %% "ciris"                     % Version.ciris
  lazy val pureconfig              = "com.github.pureconfig" %% "pureconfig"                % Version.pureConfig
  lazy val pureconfigMagnolia      = "com.github.pureconfig" %% "pureconfig-magnolia"       % Version.pureConfig
  lazy val reactivemongoBsonMacros = "org.reactivemongo"     %% "reactivemongo-bson-macros" % Version.reactivemongo
  lazy val tethysCore              = "com.tethys-json"       %% "tethys-core"               % Version.tethys
  lazy val tethysDerivation        = "com.tethys-json"       %% "tethys-derivation"         % Version.tethys
  lazy val scalacheck              = "org.scalacheck"        %% "scalacheck"                % Version.scalacheck
  lazy val sangria                 = "org.sangria-graphql"   %% "sangria"                   % Version.sangria

  lazy val circeParser   = "io.circe"        %% "circe-parser"   % Version.circe
  lazy val tethysJackson = "com.tethys-json" %% "tethys-jackson" % Version.tethys
  lazy val scalatest     = "org.scalatest"   %% "scalatest"      % Version.scalatest
  lazy val estatico      = "io.estatico"     %% "newtype"        % Version.estatico
  lazy val supertagged   = "org.rudogma"     %% "supertagged"    % Version.supertagged

  lazy val zioSchema           = "dev.zio" %% "zio-schema"            % Version.zioSchema
  lazy val zioSchemaDerivation = "dev.zio" %% "zio-schema-derivation" % Version.zioSchema
  lazy val zioJson             = "dev.zio" %% "zio-json"              % Version.zioJson

  lazy val vulcanGeneric = "com.github.fd4s" %% "vulcan-generic" % Version.vulcan

  lazy val macroParadise = "org.scalamacros" % "paradise"       % Version.macroParadise cross CrossVersion.patch
  lazy val kindProjector = "org.typelevel"  %% "kind-projector" % Version.kindProjector cross CrossVersion.patch

  def scalaReflect(scalaOrg: String, scalaVer: String) = scalaOrg % "scala-reflect" % scalaVer
}
