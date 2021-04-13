package derevo.circe.magnolia

import derevo.derive
import io.circe._
import io.circe.magnolia.configured.Configuration
import io.circe.syntax._
import io.circe.parser._
import io.estatico.newtype.macros.newtype

import org.scalatest.flatspec.AnyFlatSpec

class CirceDerivationSpec extends AnyFlatSpec {

  "Circe derivation" should "derive simple codecs" in {
    @derive(encoder, decoder)
    final case class Foo(string: String, int: Int)

    val foo     = Foo("kek", -42)
    val fooJson = """{"string":"kek","int":-42}"""

    assert(foo.asJson.noSpaces == fooJson)
    assert(decode[Foo](fooJson) == Right(foo))
  }

  it should "derive codecs with case transformation" in {
    implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

    @derive(customizableDecoder, customizableEncoder)
    final case class Bar(stringName: String, integerAge: Int)

    val decodedBar = decode[Bar]("""
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
    val bar     = SealedTrait.Bar(123)

    val bazJson =
      """
        |{
        |  "baz": "nani",
        |  "type": "Baz"
        |}
        |""".stripMargin.filterNot(_.isWhitespace)
    val baz     = SealedTrait.Baz("nani")

    val encode = Encoder[SealedTrait].apply _
    assert(encode(bar).noSpaces == barJson)
    assert(encode(baz).noSpaces == bazJson)

    assert(decode[SealedTrait](barJson) == Right(bar))
    assert(decode[SealedTrait](bazJson) == Right(baz))
  }

  it should "derive key codecs for Map data-structures" in {
    import KeyCodecs._

    val kv1     = KVStore(Map(StoreKey(1) -> StoreValue("uno"), StoreKey(2) -> StoreValue("dos")))
    val kv1Json =
      """
       |{
       |  "1" : "uno",
       |  "2" : "dos"
       |}
       |""".stripMargin.filterNot(_.isWhitespace)

    assert(Encoder[KVStore].apply(kv1).noSpaces == kv1Json)
    assert(decode[KVStore](kv1Json) == Right(kv1))

    val kv2     = SimpleKV(Map(SimpleKey("jeden") -> SimpleValue(List(1.0)), SimpleKey("dwa") -> SimpleValue(List(2.0))))
    val kv2Json =
      """
       |{
       |  "kvs" : {
       |    "jeden" : {
       |      "value" : [
       |        1.0
       |      ]
       |    },
       |    "dwa" : {
       |      "value" : [
       |        2.0
       |      ]
       |    }
       |  }
       |}
       |""".stripMargin.filterNot(_.isWhitespace)

    assert(Encoder[SimpleKV].apply(kv2).noSpaces == kv2Json)
    assert(decode[SimpleKV](kv2Json) == Right(kv2))

    val kv3     = DeadSimpleKV(Map(0 -> "zero", 1 -> "one"))
    val kv3Json =
      """
       |{
       |  "kvs" : {
       |    "0" : "zero",
       |    "1" : "one"
       |  }
       |}
       |""".stripMargin.filterNot(_.isWhitespace)

    assert(Encoder[DeadSimpleKV].apply(kv3).noSpaces == kv3Json)
    assert(decode[DeadSimpleKV](kv3Json) == Right(kv3))
  }
}

@derive(customizableEncoder, customizableDecoder)
sealed trait SealedTrait

object SealedTrait {
  implicit val configuration: Configuration = Configuration.default.withDiscriminator("type")

  @derive(encoder, decoder)
  case class Bar(bar: Int) extends SealedTrait

  @derive(encoder, decoder)
  case class Baz(baz: String) extends SealedTrait
}

object KeyCodecs {
  @derive(decoder, encoder, keyDecoder, keyEncoder)
  @newtype
  case class StoreKey(value: Int)

  @derive(decoder, encoder)
  @newtype
  case class StoreValue(value: String)

  @derive(decoder, encoder)
  @newtype
  case class KVStore(kvs: Map[StoreKey, StoreValue])

  // no newtypes here
  @derive(decoder, encoder, keyDecoder, keyEncoder)
  case class SimpleKey(value: String)

  @derive(decoder, encoder)
  case class SimpleValue(value: List[Double])

  @derive(decoder, encoder)
  case class SimpleKV(kvs: Map[SimpleKey, SimpleValue])

  @derive(decoder, encoder)
  case class DeadSimpleKV(kvs: Map[Int, String])
}
