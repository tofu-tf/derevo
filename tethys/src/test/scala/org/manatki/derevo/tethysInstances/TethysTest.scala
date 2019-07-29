package org.manatki.derevo.tethysInstances

import org.manatki.derevo.derive
import org.scalatest.{FlatSpec, Matchers}
import tethys._
import tethys.jackson._

@derive(tethysWriter)
sealed trait Choice

@derive(tethysWriter)
final case class First(a: Int, b: String) extends Choice

@derive(tethysWriter)
final case class Second(c: Boolean, f: Option[Foo]) extends Choice

@derive(tethysWriter)
final case class P[A, B](a: A, b: List[B])

case object Third extends Choice {
  implicit val writer: JsonObjectWriter[Third.type] = (_, _) => ()
}

@derive(tethysWriter, tethysReader)
case class Foo(d: Int, e: String)

@derive(tethysWriter)
case class ChoiceList(list: List[Choice], amount: Int)

class TethysTest extends FlatSpec with Matchers {
  "Writer derivation for ADT" should "work correctly" in {
    val choices = ChoiceList(List(First(1, "lol"), Second(c = true, Some(Foo(1, "kek"))), Third), 3)

    choices.asJson shouldBe """{"list":[{"a":1,"b":"lol"},{"c":true,"f":{"d":1,"e":"kek"}},{}],"amount":3}"""
  }

  "Reader derivation for case class" should "work correctly" in {
    val foo = Foo(100, "kek")

    """{"d":100,"e":"kek"}""".jsonAs[Foo] shouldBe Right(foo)
  }

  "Parametric derivation for case class" should "work correctly" in {
    val p = P(123.0, List(1, 2, 3))

    p.asJson shouldBe """{"a":123.0,"b":[1,2,3]}"""
  }
}
