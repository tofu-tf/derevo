# derevo
Multiple instance derivations inside a single macro annotation

| CI | Release |
| --- | --- |
| [![Build Status](https://travis-ci.org/manatki/derevo.svg?branch=master)](https://travis-ci.org/manatki/derevo) | [![Maven Central](https://img.shields.io/maven-central/v/org.manatki/derevo-core_2.12.svg)](https://search.maven.org/search?q=derevo) |

## Breaking changes in 0.11
`org.manatki.derevo` pkg was shortened to `derevo`.

Use [scalafix](https://scalacenter.github.io/scalafix/docs/users/installation) and [this rule](https://gist.github.com/REDNBLACK/9bc56ad71e4b01a63001339fa61b4cfd) for migration

## Installation
For Scala 2.12 and older:
```sbt
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
```

For Scala 2.13:
```sbt
scalacOptions += "-Ymacro-annotations"
```

## IntelliJ Integration
Provides full support and visibility of implicits declared in `@derive` annotation.

To activate, simply click 'Yes' on the extensions popup, after adding any of the `derevo-` integration libraries to your project.
![](https://i.imgur.com/E6BKTeH.png)

## Supported integrations

### [Cats](https://github.com/typelevel/cats)
```sbt
libraryDependencies += "org.manatki" %% "derevo-cats" % "latest version in badge"
```

```scala
import derevo.derive
import derevo.cats.{eq => eqv, show, order, monoid}

import cats.Monoid
import cats.instances.string._
import cats.instances.int._
import cats.syntax.show._
import cats.syntax.order._
import cats.syntax.semigroup._
import cats.syntax.monoid._

@derive(eqv, show, order, monoid)
case class Foo(bar: String, baz: Int)

assert(Foo("111", 222) === Foo("111", 222))
assert(show"${Foo("111", 222)}" === "Foo{bar=111,baz=222}")
assert((Foo("111", 222) compare Foo("222", 333)) == -1)
assert((Foo("1", 1) |+| Foo("2", 2)) == Foo("12", 3))
assert(Monoid[Foo].empty == Foo("", 0))
```

### [Cats Tagless](https://github.com/typelevel/cats-tagless)
```sbt
libraryDependencies += "org.manatki" %% "derevo-cats-tagless" % "latest version in badge"
```

```scala
import derevo.derive
import derevo.tagless.{functor, flatMap, invariant, contravariant, functorK, invariantK, semigroupalK, applyK}

// TODO
```

### [Tethys](https://github.com/tethys-json/tethys)
```sbt
libraryDependencies += "org.manatki" %% "derevo-tethys" % "latest version in badge"
```

```scala
import derevo.derive
import derevo.tethys.{tethysReader, tethysWriter}

import tethys._
import tethys.derivation.builder.{FieldStyle, WriterDerivationConfig}
import tethys.jackson._

@derive(
  tethysReader,
  tethysWriter(WriterDerivationConfig.withFieldStyle(FieldStyle.lowerSnakecase))
)
final case class Bar(stringName: String, integerAge: Int)

assert(Bar("Cotique", 27).asJson == """{"string_name":"Cotique","integer_age":27}""")
assert("""{"stringName":"Elya","integerAge":32}""".jsonAs[Bar] == Right(Bar("Elya", 32)))
```

### [Circe](https://github.com/circe/circe)
```sbt
libraryDependencies += "org.manatki" %% "derevo-circe" % "latest version in badge"
```

```scala
import derevo.derive
import derevo.circe.{decoder, encoder}

import io.circe._
import io.circe.syntax._
import io.circe.parser._

@derive(decoder, encoder)
final case class Bar(stringName: String, integerAge: Int)

assert(Bar("KKK", 22).asJson.printWith(Printer.noSpaces) == """{"stringName":"KKK","integerAge":22}""")
assert(parse("""{"stringName":"WWW","integerAge":20}""").flatMap(_.as[Bar]) == Right(Bar("WWW", 20)))
```

### [Circe Magnolia](https://github.com/circe/circe-magnolia)
```sbt
libraryDependencies += "org.manatki" %% "derevo-circe-magnolia" % "latest version in badge"
```

```scala
import derevo.derive
import derevo.circe.magnolia.{decoder, encoder}

import io.circe._
import io.circe.syntax._
import io.circe.parser._

@derive(decoder, encoder)
final case class Bar(stringName: String, integerAge: Int)

assert(Bar("KKK", 22).asJson.printWith(Printer.noSpaces) == """{"stringName":"KKK","integerAge":22}""")
assert(parse("""{"stringName":"WWW","integerAge":20}""").flatMap(_.as[Bar]) == Right(Bar("WWW", 20)))
```

To change default `io.circe.magnolia.configured.Configuration`:
```
import derevo.derive
import derevo.circe.magnolia.{customizableDecoder, customizableEncoder}

import io.circe._
import io.circe.syntax._
import io.circe.parser._

@derive(customizableEncoder, customizableDecoder)
sealed trait SealedTrait

object SealedTrait {
  implicit val configuration:Configuration = Configuration.default.withDiscriminator("type")

  @derive(encoder, decoder)
  case class Bar(bar: Int) extends SealedTrait

  @derive(encoder, decoder)
  case class Baz(baz: String) extends SealedTrait
}
```

### [Ciris](https://github.com/vlovgr/ciris) + `HOCON`
```sbt
libraryDependencies += "org.manatki" %% "derevo-ciris" % "latest version in badge"
```

```scala
import derevo.derive
import derevo.ciris.cirisDecoder

import com.typesafe.config.ConfigFactory
import ciris.hocon._
import ciris.hocon.instances._

@derive(cirisDecoder)
case class DataConfig(name: String, addresses: List[String], mapping: Map[String, Int])

val source = hoconSource[DataConfig](
    ConfigFactory.parseString(
    """
      |data {
      |  name = AAA
      |  addresses = [home, work, pub]
      |  mapping.until = 1
      |  mapping.from  = 2
      |  mapping.to    = 3
      |}
      """.stripMargin
    ),
    "data"
)

// Load in F[_] context, cats.effect.IO just for example
import cats.effect.IO
import scala.concurrent.ExecutionContext.global

implicit val cs = IO.contextShift(global)

assert(source.load[IO].unsafeRunSync() == DataConfig("AAA", List("pub", "home", "work"), Map("until" -> 1, "from" -> 2, "to" -> 3)))
```

### [PureConfig](https://github.com/pureconfig/pureconfig)
```sbt
libraryDependencies += "org.manatki" %% "derevo-pureconfig" % "latest version in badge"
```

```scala
import derevo.derive
import derevo.pureconfig.{pureconfigReader, pureconfigWriter}

import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.syntax._

@derive(pureconfigReader, pureconfigWriter)
case class DataConfig(name: String, addresses: List[String], mapping: Map[String, Int])

val raw = ConfigFactory
  .parseString(
    """
      |{
      |  name = AAA
      |  addresses = [home, work, pub]
      |  mapping.until = 1
      |  mapping.from  = 2
      |  mapping.to    = 3
      |}
        """.stripMargin
  )

val parsed = ConfigSource.fromConfig(raw).load[DataConfig]

assert(parsed == Right(DataConfig("AAA", List("home", "work", "pub"), Map("until" -> 1, "from" -> 2, "to" -> 3))))
assert(parsed.map(_.toConfig.atPath("data").getConfig("data")) == Right(raw))
```

### [TypedSchema](https://github.com/TinkoffCreditSystems/typed-schema)
```sbt
libraryDependencies += "org.manatki" %% "derevo-tschema" % "latest version in badge"
```

```scala
import derevo.derive
import derevo.tschema.{swagger, openapiParam, httpParam}

// TODO
```

### [RMongo](https://github.com/tc/RMongo)
```sbt
libraryDependencies += "org.manatki" %% "derevo-rmongo" % "latest version in badge"
```

```scala
import derevo.derive
import derevo.reactivemongo.{bsonDocumentReader, bsonDocumentWriter}

// TODO
```

### [Scalacheck](https://github.com/typelevel/scalacheck)
```sbt
libraryDependencies += "org.manatki" %% "derevo-scalacheck" % "latest version in badge" % Test
```

```scala
import derevo.derive
import org.scalacheck.Arbitrary
import derevo.scalacheck.arbitrary

// for existing classes
sealed trait Bear
case class Beer(b: String, t: Boolean) extends Bear
case class Gear(g: Int) extends Bear

println(arbitrary.instance[Bear].arbitrary.sample)

// for classes in tests
@derive(arbitrary)
case class Test(x: String, y: List[Int])

println(implicitly[Arbitrary[Test]].arbitrary.sample)
```