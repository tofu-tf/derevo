package derevo.zio.json

import derevo.derive
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import zio.json.JsonCodecConfiguration.SumTypeHandling
import zio.json._

@derive(jsonEncoder)
sealed trait Choice

object Choice {
  implicit val Configuration: JsonCodecConfiguration =
    JsonCodecConfiguration(SumTypeHandling.DiscriminatorField("type"))
}

@derive(jsonEncoder)
final case class First(a: Int, b: String) extends Choice

@derive(jsonEncoder)
final case class Second(c: Boolean, f: Option[Foo]) extends Choice

@derive(jsonEncoder)
final case class P[A, B](a: A, b: List[B])

@derive(jsonEncoder, jsonDecoder)
case class Foo(d: Int, e: String)

@derive(jsonEncoder)
case class ChoiceList(list: List[Choice], amount: Int)

class ZioJsonSpec extends AnyFlatSpec with Matchers {
  "Encoder derivation for ADT" should "work correctly" in {
    val choices = ChoiceList(List(First(1, "lol"), Second(c = true, Some(Foo(1, "kek")))), 2)

    choices.toJson shouldBe """{"list":[{"type":"First","a":1,"b":"lol"},{"type":"Second","c":true,"f":{"d":1,"e":"kek"}}],"amount":2}"""
  }

  "Decoder derivation for case class" should "work correctly" in {
    val foo = Foo(100, "kek")

    """{"d":100,"e":"kek"}""".fromJson[Foo] shouldBe Right(foo)
  }

  "Parametric derivation for case class" should "work correctly" in {
    val p = P(123.0, List(1, 2, 3))

    p.toJson shouldBe """{"a":123.0,"b":[1,2,3]}"""
  }
}
