package derevo
package cats

import _root_.cats.Show
import _root_.cats.implicits._
import _root_.cats.Comparison
import org.scalatest._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

@derive(show, order, monoid)
case class Foo(bar: String, baz: Int)

@derive(show, order, monoid)
case class Bar[T, Q, @derevo.phantom R](x: T, qs: List[Q])

@derive(show)
case class Lol(x: X)

@derive(showNewline)
case class Big(x: X, y: Y, z: Option[String])


class X

class Y

object Lol {
  implicit private[this] val xShow: Show[X] = (_: X) => "X"
  implicit private[this] val yShow: Show[Y] = (_: Y) => "Y"

  insertInstancesHere()

  val shown: String = Lol(new X).show
}


object Big {
  implicit private[this] val xShow: Show[X] = (_: X) => "X"
  implicit private[this] val yShow: Show[Y] = (_: Y) => "Y"

  insertInstancesHere()

  val shown: String = Big(new X, new Y, Some("z")).show
}

class CatsTest extends AnyFunSpec with Matchers {

  describe("Cats derivation") {
    describe("for Order typeclass") {
      it("shoud derive instance with order using default cats instances") {
        Foo("bbb", 1) comparison Foo("aaa", 2) shouldBe Comparison.GreaterThan
        Foo("bbb", 1) comparison Foo("bbb", 2) shouldBe Comparison.LessThan
        Foo("bbb", 1) comparison Foo("bbb", 1) shouldBe Comparison.EqualTo
      }
    }
    describe("for Monoid typeclass") {
      it("shoud derive instance with monoid using default cats instances") {
        Foo("aaa", 2) |+| Foo("bbb", 3) shouldBe Foo("aaabbb", 5)
      }
      it("shoud derive instance with monoid using default cats instances when class has type parameters") {
        Bar[Int, String, Any](1, List("a", "b")) |+| Bar(3, List("c")) shouldBe Bar(4, List("a", "b", "c"))
      }
    }
    describe("for Show typeclass") {
      it("should derive instance with show using instances from companion object") {
        Lol.shown shouldBe "Lol{x=X}"
      }
      it("should derive with show instance using default cats instances") {
        Foo("lol", 3).show shouldBe "Foo{bar=lol,baz=3}"
      }

      it("should derive instance with showNewline with newline delimited fields") {
        Big.shown shouldBe
          """Big{
            |  x = X,
            |  y = Y,
            |  z = Some(z)
            |}""".stripMargin
      }

      it("should derive instance with show with newline delimited fields when class has type parameters") {
        Bar[Int, String, Any](2, List("lol", "kek")).show shouldBe "Bar{x=2,qs=List(lol, kek)}"
      }
    }
  }
}
