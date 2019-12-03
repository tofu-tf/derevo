package org.manatki.derevo.scalacheckDerivation

import org.manatki.derevo.derive
import org.scalacheck.Arbitrary

@derive(arbitrary)
case class Foo(bar: String, baz: Int)

sealed trait Lol

@derive(arbitrary)
case class Kek(bar: Int) extends Lol

@derive(arbitrary)
case class Bek(baz: String) extends Lol

object arbitraryTest {
  def main(args: Array[String]): Unit = {
    println(implicitly[Arbitrary[Foo]].arbitrary.sample)
    val lolGen = arbitrary.instance[Lol].arbitrary
    println((0 until 10).map(_ => lolGen.sample).mkString("\n"))
  }
}
