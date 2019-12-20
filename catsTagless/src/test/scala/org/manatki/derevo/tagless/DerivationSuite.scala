package org.manatki.derevo.tagless

import cats.arrow.FunctionK
import cats.arrow.FunctionK.lift
import cats.data.Tuple2K
import cats.tagless.{ApplyK, Derive}
import org.manatki.derevo.derive
import cats.tagless.syntax.all._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.invariant._
import cats.syntax.contravariant._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

@derive(functorK, semigroupalK)
trait Foo[F[_]] {
  def foo(name: String): F[Char]
}

@derive(applyK)
trait Bar[F[_]] {
  def bar(x: String, y: String): F[String]
}

@derive(invariantK)
trait Sum3[F[_]] {
  def sum3[A](x: F[A], y: F[A], z: F[A]): F[A]
}

@derive(flatMap)
trait Baz[X] {
  def baz(x: Int): X
}

@derive(functor)
trait Quux[X] {
  def quux(x: Int): X
}

@derive(contravariant)
trait Chepok[X] {
  def chepok(x: X): Int
}

@derive(invariant)
trait Tento[X] {
  def tento(x: X): X
}


class DerivationSuite extends AnyFlatSpec with Matchers {
  val listFoo: Foo[List]                       = name => name.toList
  val funFoo: Foo[Int => ?]                    = name => i => name(i)
  val optFoo: Foo[Option]                      = listFoo.mapK(functionK[List](_.headOption))
  val prodFoo: Foo[Tuple2K[List, Int => ?, ?]] = listFoo.productK[Int => ?](funFoo)

  val intBar: Bar[Int => ?]       = (x, y) => i => s"$x [$i] $y"
  val stringBar: Bar[String => ?] = (x, y) => s => s"$x {$s} $y"
  val intEStringBar: Bar[Either[Int, String] => ?] =
    ApplyK[Bar].map2K[Int => ?, String => ?, Either[Int, String] => ?](intBar, stringBar)(
      functionK[Tuple2K[Int => ?, String => ?, ?]][Either[Int, String] => ?](fab => e => e.fold(fab.first, fab.second)))

  "Simple FunctorK" should "apply FunctionK" in {
    optFoo.foo("hello") shouldBe Some('h')
    optFoo.foo("") shouldBe None
  }

  val vectorSum3: Sum3[Vector] = new Sum3[Vector] {
    def sum3[A](x: Vector[A], y: Vector[A], z: Vector[A]): Vector[A] = x ++ y ++ z
  }

  def vectorToList[A](xs: Vector[A]) = xs.toList
  def listToVector[A](xs: List[A])   = xs.toVector
  val listSum3: Sum3[List]           = vectorSum3.imapK(lift(vectorToList))(lift(listToVector))

  "Simple InvariantK" should "apply nat isomorphism" in {
    listSum3.sum3(List(1, 2), Nil, List(3, 4, 5)) shouldBe List(1, 2, 3, 4, 5)
  }

  "Simple SemigroupalK" should "combine results" in {
    prodFoo.foo("hello").first shouldBe "hello".toList
    prodFoo.foo("hello").second(2) shouldBe 'l'
  }

  "Simple ApplyK" should "combine results" in {
    intEStringBar.bar("one", "two")(Left(12)) shouldBe "one [12] two"
    intEStringBar.bar("one", "three")(Right("two")) shouldBe "one {two} three"
  }

  "Simple flatMap" should "do for complehensions" in {
    val res: Baz[String] = for {
      x <- (a => a * 2): Baz[Int]
      y <- (a => a + x): Baz[Double]
      z <- (a => "s" * a): Baz[String]
    } yield s"$x $y $z"

    res.baz(3) shouldBe "6 9.0 sss"
  }

  "Simple functor" should "map values" in {
    ((x => x * 3) : Quux[Int]).map(_.toString).quux(23) shouldBe "69"
  }

  "Simple contravarian" should "contramap values" in {
    ((x => x * 3): Chepok[Int]).contramap[String](_.toInt).chepok("23") shouldBe 69
  }

  "Simple invariant" should "imap values" in {
    ((x => x * 3): Tento[Int]).imap(_.toString)(_.toInt).tento("23") shouldBe "69"
  }

}
