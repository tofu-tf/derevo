package derevo.tethys

import derevo.derive
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import tethys._
import tethys.jackson._

@derive(jsonWriter, jsonReader)
sealed trait Choice

final case class First(a: Int, b: String) extends Choice

final case class Second(c: Boolean, f: Option[Foo]) extends Choice

case object Third extends Choice

@derive(jsonWriter, jsonReader)
sealed trait ChoiceX

final case class FirstX(a: Int, b: String) extends ChoiceX

final case class SecondX(c: Boolean, f: Option[Foo]) extends ChoiceX

case object ThirdX extends ChoiceX

object Choice {
  implicit val jsonCfg: CodecConfig = CodecConfig.default.withDiscriminator("type")
}

object ChoiceX {
  implicit val jsonCfg: CodecConfig = CodecConfig.default.copy(
    constrRename = _.replace("X", "")
  )
}

@derive(jsonWriter)
final case class P[A, B](a: A, b: List[B])

@derive(jsonWriter, jsonReader)
case class Foo(d: Int, e: String)

@derive(jsonWriter, jsonReader)
case class ChoiceList(list: List[Choice], amount: Int)

@derive(jsonReader, jsonWriter)
case class ChoiceXList(list: List[ChoiceX], amount: Int)

class TethysSuite extends AnyFlatSpec with Matchers {
  val choices      = ChoiceList(List(First(1, "lol"), Second(c = true, Some(Foo(1, "kek"))), Third), 3)
  val choicesX     = ChoiceXList(List(FirstX(1, "lol"), SecondX(c = true, Some(Foo(1, "kek"))), ThirdX), 3)
  val choicesJson  =
    """{"list":[{"type":"First","a":1,"b":"lol"},{"type":"Second","c":true,"f":{"d":1,"e":"kek"}},{"type":"Third"}],"amount":3}"""
  val choicesXJson =
    """{"list":[{"First":{"a":1,"b":"lol"}},{"Second":{"c":true,"f":{"d":1,"e":"kek"}}},{"Third":{}}],"amount":3}"""

  "Writer derivation for ADT with discriminator" should "work correctly" in {
    choices.asJson shouldBe choicesJson
  }

  "Reader derivation for ADT with discriminator" should "work correctly" in {
    choicesJson.jsonAs[ChoiceList] shouldBe Right(choices)
  }

  "Writer derivation for ADT without discriminator" should "work correctly" in {
    choicesX.asJson shouldBe choicesXJson
  }

  "Reader derivation for ADT without discriminator" should "work correctly" in {
    choicesXJson.jsonAs[ChoiceXList] shouldBe Right(choicesX)
  }

  "Reader derivation for case class" should "work correctly" in {
    val foo = Foo(100, "kek")

    """{"d":100,"e":"kek"}""".jsonAs[Foo] shouldBe Right(foo)
  }

  "Parametric derivation for case class" should "work correctly" in {
    val p = P(123.0, List(1, 2, 3))

    p.asJson shouldBe """{"a":123.0,"b":[1,2,3]}"""
  }

  it should "derive codecs with case transformation" in {
    @derive(jsonReader, jsonWriter)
    final case class Bar(stringName: String, integerAge: Int)

    object Bar {
      implicit val jsonConf: CodecConfig = CodecConfig.default.snakecase
    }

    val decodedBar = """
        |{
        |   "string_name": "Cheburek",
        |   "integer_age": 146
        |}
        |""".stripMargin.jsonAs[Bar]

    assert(decodedBar == Right(Bar("Cheburek", 146)))
    val encodedBar =
      """
        |{
        |   "string_name": "Lolkek",
        |   "integer_age": -228
        |}
        |""".stripMargin.filterNot(_.isWhitespace)
    assert(Bar("Lolkek", -228).asJson == encodedBar)
  }
}
