package derevo.vulcan

import org.apache.avro.Schema
import derevo.derive
import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import vulcan.Codec
import vulcan.generic.{AvroDoc, AvroName}

import java.time.Instant

@derive(avroCodec)
case class Foo(i: Int, s: String, bar: Bar)

@derive(avroCodec)
case class Bar(
    @AvroName("time_dttm") @AvroDoc("Timestamp")
    timestamp: Instant,
    @AvroName("int_list") @AvroDoc("List of ints")
    intList: List[Int]
)

class AvroCodecTest extends AnyFlatSpec with Matchers {

  val fooSchema: String =
    """{"type":"record","name":"Foo","namespace":"derevo.vulcan","fields":[{"name":"i","type":"int"},{"name":"s","type":"string"},{"name":"bar","type":{"type":"record","name":"Bar","fields":[{"name":"time_dttm","type":{"type":"long","logicalType":"timestamp-millis"},"doc":"Timestamp"},{"name":"int_list","type":{"type":"array","items":"int"},"doc":"List of ints"}]}}]}"""

  def assertSchema[A: Codec](schemaStr: String): Assertion = {
    val expectedSchema = new Schema.Parser().parse(schemaStr.mkString.trim)
    Codec[A].schema shouldBe Right(expectedSchema)
  }

  "Writer derivation for ADT" should "work correctly" in {
    assertSchema[Foo](fooSchema)
  }
}
