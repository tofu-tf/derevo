# derevo

[![Build & Release](https://github.com/tofu-tf/derevo/workflows/Scala%20CI/badge.svg)](https://github.com/tofu-tf/derevo/actions?query=workflow%3A%22Scala+CI%22)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/tf.tofu/derevo-core_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/tf.tofu/derevo-core_2.13)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)


Multiple instance derivations inside a single macro annotation.


## Basic Usage
If a library has support for `derevo`, you can simply write
```scala
@derive(SomeCaseClass, AnotherCaseClass, fooDerivation, barDerivation(param1, param2))
case class Foo(...)
```
where `SomeCaseClass`, `AnotherCaseClass`, `fooDerivation`, `barDerivation` are some objects, extending one of the `InstanceDef` traits ([see module source](https://github.com/tofu-tf/derevo/blob/supertagged/core/src/main/scala/derevo/package.scala#L13:L21)).

For every element of the `@derive` macro, there will be generated `implicit val` or `implicit def` (when `Foo` has type parameters) providing the corresponding typeclass.
For simple type derivation, if `Foo` has type parameters, the instance will require an instance of the same or specified other typeclass for proper derivation.
Example:
```scala
@derive(encoder)
case class Foo[@phantom A, B](...)

// `derive` generates:
implicit def encoder$macro$1[A, B: Encoder]: Encoder.AsObject[Foo[A, B]] = ...
```

`Foo` can also be a newtype in form of [estatico](https://github.com/estatico/scala-newtype) or [supertagged](https://github.com/rudogma/scala-supertagged) libraries.

If you have problems with the initialization order you can optionally put the
`insertInstancesHere()` call to the body of your companion object to specify the place where `implicit val`s should be inserted.


## Making your own derivation.
First, extend the object (the companion object for your type is the best option) from one of the [`InstanceDef` traits](https://github.com/tofu-tf/derevo/blob/supertagged/core/src/main/scala/derevo/package.scala#L13:L21).

Then implement the `instance` method for your object so it could derive the corresponding instance.
Example:
```scala
trait TypeClass[A] {
...
}

object TypeClass extends Derivaton[TypeClass] {
  def instance[A]: TypeClass[A] = ...
}
```

Also, you can define additional methods `def apply(...)` or `def foo(...)` in your derivation object. This will allow instance creation as
`@derive(TypeClass(...))` or `@derive(TypeClass.foo(...))`.

To support `newtype` derivation, extend your object with (`NewtypeDerivation`)[https://github.com/tofu-tf/derevo/blob/master/core/src/main/scala/derevo/NewTypeRepr.scala#L8]. Alternatively, your object may have the `newtype[R]` method with a single type parameter that receives the underlying type and returns another object that has the `instance` method that works as described earlier.

Sometimes, the required constraint may differ from the provided typeclass, e.g. `circe.Encoder` which requires
`Encoder` for each field but provides `Encoder.AsObject` for the target case class.
In this case you should extend `SpecificDerivation` instead, like `object foo extends SpecificDerivation[FromTc, ToTc, NT]`, where

 - `FromTc[A]` - typeclass that will be required for non-phantom type parameters
 - `ToTc[A]` - normal typeclass provided by the instance
 - `NT[A]` - type class that will be forwarded by the newtype derivation


## Expression table

| Derivation line                                           | Translation                                                |
|-----------------------------------------------------------|------------------------------------------------------------|
| `@derive(...foo, ...)`                                    | `implicit val foo$macro: Foo[A] = foo.instance`            |
| `@derive(...foo(bar))`                                    | `implicit val foo$macro : Foo[A] = foo(bar)`               |
| `@derive(...foo.quux(bar))`                               | `implicit val foo$macro : Foo[A] = foo.quux(bar)`          |
| `@derive(...foo, ...)` when `A` is a newtype over `B`     | `implicit val foo$macro: Foo[A] = foo.newtype[B].instance` |


## Delegating to other macros
`Derevo` supports macro delegation when the corresponding macro function is provided by another library.

This works by adding the `@delegating` annotation to your delegator object:
```scala
@delegating("full.qualified.method.path")
```
Then call `macro Derevo.delegate`, `macro Derevo.delegateParams`, `macro Derevo.delegateParams2` or `macro Derevo.delegateParams3` in the corresponding methods.
An example can be found [here](https://github.com/tofu-tf/derevo/blob/supertagged/circe/src/main/scala/derevo/circe/circe.scala).


## Installation
For Scala 2.12 and older:
```sbt
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
```

For Scala 2.13:
```sbt
scalacOptions += "-Ymacro-annotations"
```

## Supported integrations

### [Cats](https://github.com/typelevel/cats)
```sbt
libraryDependencies += "tf.tofu" %% "derevo-cats" % "latest version in badge"
```

```scala
import derevo.derive
import derevo.cats.{eqv, show, order, monoid}

import cats.Monoid
import cats.instances.string._
import cats.instances.int._
import cats.syntax.show._
import cats.syntax.order._
import cats.syntax.semigroup._
import cats.syntax.monoid._

@derive(eqv, show, order, monoid)
case class Foo(bar: String, baz: Int)
@derive(eqv.universal)
case class Bar(x: Int)

assert(Foo("111", 222) === Foo("111", 222))
assert(Bar(1) === Bar(1))
assert(show"${Foo("111", 222)}" === "Foo{bar=111,baz=222}")
assert((Foo("111", 222) compare Foo("222", 333)) == -1)
assert((Foo("1", 1) |+| Foo("2", 2)) == Foo("12", 3))
assert(Monoid[Foo].empty == Foo("", 0))
```

### [Cats Tagless](https://github.com/typelevel/cats-tagless)
```sbt
libraryDependencies += "tf.tofu" %% "derevo-cats-tagless" % "latest version in badge"
```

```scala
import derevo.derive
import derevo.tagless.{functor, flatMap, invariant, contravariant, functorK, invariantK, semigroupalK, applyK}

// TODO
```

### [Tethys](https://github.com/tethys-json/tethys)
```sbt
libraryDependencies += "tf.tofu" %% "derevo-tethys" % "latest version in badge"
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
libraryDependencies += "tf.tofu" %% "derevo-circe" % "latest version in badge"
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
libraryDependencies += "tf.tofu" %% "derevo-circe-magnolia" % "latest version in badge"
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
libraryDependencies += "tf.tofu" %% "derevo-ciris" % "latest version in badge"
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
libraryDependencies += "tf.tofu" %% "derevo-pureconfig" % "latest version in badge"
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

### [RMongo](https://github.com/tc/RMongo)
```sbt
libraryDependencies += "tf.tofu" %% "derevo-rmongo" % "latest version in badge"
```

```scala
import derevo.derive
import derevo.reactivemongo.{bsonDocumentReader, bsonDocumentWriter}

// TODO
```

### [Scalacheck](https://github.com/typelevel/scalacheck)
```sbt
libraryDependencies += "tf.tofu" %% "derevo-scalacheck" % "latest version in badge" % Test
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
