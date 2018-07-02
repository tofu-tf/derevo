package org.manatki.derevo
package catsInstances

import cats.implicits._

@derive(show, order, monoid)
case class Foo(bar: String, baz: Int)


class CatsSuite {
  def main(args: Array[String]): Unit = {
    println(show" === ${Foo("lol", 3)} ===")

    println(Foo("bbb", 1) comparison Foo("aaa", 2))
    println(Foo("bbb", 1) comparison Foo("bbb", 2))
    println(Foo("bbb", 1) comparison Foo("bbb", 1))

    println(Foo("aaa", 2) |+| Foo("bbb", 3))
  }
}