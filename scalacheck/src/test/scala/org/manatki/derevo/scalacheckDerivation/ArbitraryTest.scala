package org.manatki.derevo.scalacheckDerivation

import org.manatki.derevo.derive
import org.scalacheck.Arbitrary

@derive(arbitrary)
case class Foo(bar: String, baz: Int)

sealed trait Lol
case class Kek(bar: Int)          extends Lol
case class Bek(baz: List[String]) extends Lol

object ArbitraryTest {
  def main(args: Array[String]): Unit = {
    println(implicitly[Arbitrary[Foo]].arbitrary.sample)
    val lolGen = arbitrary.instance[Lol].arbitrary
    println((0 until 10).map(_ => lolGen.sample).mkString("\n"))
  }
}
