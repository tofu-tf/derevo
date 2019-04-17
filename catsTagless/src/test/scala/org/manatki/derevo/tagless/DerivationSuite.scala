package org.manatki.derevo.tagless

import cats.arrow.FunctionK
import org.manatki.derevo.derive
import org.scalatest.{FlatSpec, Matchers}
import cats.tagless.syntax.functorK._

@derive(functorK)
trait Foo[F[_]] {
  def foo(name: String): F[Char]
}

class FooSuite extends FlatSpec with Matchers {
  val listFoo: Foo[List]          = name => name.toList
  def headOption[A](xs: List[A])  = xs.headOption
  val optFoo: Foo[Option]         = listFoo.mapK(FunctionK.lift(headOption))

  "Simple FunctorK" should "apply FunctionK" in {
    optFoo.foo("hello") shouldBe Some('h')
    optFoo.foo("") shouldBe None
  }
}
