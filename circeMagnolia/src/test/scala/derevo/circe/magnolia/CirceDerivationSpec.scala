package derevo.circe.magnolia

import derevo.derive
import io.circe._
import io.circe.magnolia.configured.Configuration
import io.circe.syntax._
import io.circe.parser._
//import io.circe.derivation.renaming

import org.scalatest.flatspec.AnyFlatSpec

class CirceDerivationSpec extends AnyFlatSpec {

  "Circe derivation" should "derive simple codecs" in {
    @derive(encoder, decoder)
    final case class Foo (string: String, int: Int)

    val foo = Foo("kek", -42)
    val fooJson = """{"string":"kek","int":-42}"""

    assert(foo.asJson.noSpaces == fooJson)
    assert(decode[Foo](fooJson) == Right(foo))
  }

  it should "derive codecs with case transformation" in {
    implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

    @derive(customizableDecoder, customizableEncoder)
    final case class Bar(stringName: String, integerAge: Int)

    val decodedBar = decode[Bar](
      """
        |{
        |   "string_name": "Cheburek",
        |   "integer_age": 146
        |}
        |""".stripMargin)

    assert(decodedBar == Right(Bar("Cheburek", 146)))
    val encodedBar =
      """
        |{
        |   "string_name": "Lolkek",
        |   "integer_age": -228
        |}
        |""".stripMargin.filterNot(_.isWhitespace)
    assert(Bar("Lolkek", -228).asJson.noSpaces == encodedBar)
  }

  it should "derive codecs with type discriminator" in {
    val barJson =
      """
        |{
        |  "bar": 123,
        |  "type": "Bar"
        |}
        |""".stripMargin.filterNot(_.isWhitespace)
    val bar = SealedTrait.Bar(123)

    val bazJson =
      """
        |{
        |  "baz": "nani",
        |  "type": "Baz"
        |}
        |""".stripMargin.filterNot(_.isWhitespace)
    val baz = SealedTrait.Baz("nani")

    val encode = Encoder[SealedTrait].apply _
    assert(encode(bar).noSpaces == barJson)
    assert(encode(baz).noSpaces == bazJson)

    assert(decode[SealedTrait](barJson) == Right(bar))
    assert(decode[SealedTrait](bazJson) == Right(baz))
  }
}

@derive(customizableEncoder, customizableDecoder)
sealed trait SealedTrait

object SealedTrait {
  implicit val configuration:Configuration = Configuration.default.withDiscriminator("type")

  @derive(encoder, decoder)
  case class Bar(bar: Int) extends SealedTrait

  @derive(encoder, decoder)
  case class Baz(baz: String) extends SealedTrait
}
