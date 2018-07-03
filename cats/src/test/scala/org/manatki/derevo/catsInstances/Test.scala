package org.manatki.derevo
package catsInstances

import cats.implicits._

@derive(show, order, monoid)
case class Foo(bar: String, baz: Int)

@derive(show, order, monoid)
case class Bar[T, Q, @org.manatki.derevo.phantom R](x: T, qs: List[Q])

object CatsTest {
  def main(args: Array[String]): Unit = {
    println(show" === ${Foo("lol", 3)} ===")

    println(Foo("bbb", 1) comparison Foo("aaa", 2))
    println(Foo("bbb", 1) comparison Foo("bbb", 2))
    println(Foo("bbb", 1) comparison Foo("bbb", 1))

    println(Foo("aaa", 2) |+| Foo("bbb", 3))

    println(show"  === ${Bar[Int, String, Any](2, List("lol", "kek"))} === ")

    println(Bar[Int, String, Any](1, List("a", "b")) |+| Bar(3, List("c")))
  }
}
