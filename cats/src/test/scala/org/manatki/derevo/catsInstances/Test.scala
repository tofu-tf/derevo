package org.manatki.derevo
package catsInstances

import cats.implicits._

@derive(catsShow)
case class Foo(bar: String, baz: Int)


object Test {
  def main(args: Array[String]): Unit = {
    println(Foo)
//    val u: Show[Foo] = catsShow.instance(())
    println(Foo("lol", 3).show)
  }
}