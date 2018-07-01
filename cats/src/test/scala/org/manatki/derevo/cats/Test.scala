package org.manatki.derevo
package cats

import _root_.cats.syntax.show._

@derive(show)
case class Foo(bar: String, baz: Int)


object Test {
  def main(args: Array[String]): Unit = {
    println(Foo)
  }
}