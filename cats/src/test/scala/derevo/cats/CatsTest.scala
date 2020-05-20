package derevo
package cats

import _root_.cats.Show
import _root_.cats.implicits._

@derive(show, order, monoid)
case class Foo(bar: String, baz: Int)

@derive(show, order, monoid)
case class Bar[T, Q, @derevo.phantom R](x: T, qs: List[Q])

@derive(show)
case class Lol(x: X)

class X

object Lol {
  implicit private[this] val xShow: Show[X] = (_: X) => "X"

  insertInstancesHere()

  val shown = Lol(new X).show
}

object CatsTest {
  def main(args: Array[String]): Unit = {
    println(show" === ${Foo("lol", 3)} ===")

    println(Foo("bbb", 1) comparison Foo("aaa", 2))
    println(Foo("bbb", 1) comparison Foo("bbb", 2))
    println(Foo("bbb", 1) comparison Foo("bbb", 1))

    println(Foo("aaa", 2) |+| Foo("bbb", 3))

    println(show"  === ${Bar[Int, String, Any](2, List("lol", "kek"))} === ")

    println(Bar[Int, String, Any](1, List("a", "b")) |+| Bar(3, List("c")))

    println(Lol.shown)
  }
}
