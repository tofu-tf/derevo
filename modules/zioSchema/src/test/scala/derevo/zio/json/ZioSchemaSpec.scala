package derevo.zio.json

import derevo.derive
import derevo.zio.schema.schema
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import zio.schema._

@derive(schema)
final case class Person(name: String, age: Int)

class ZioSchemaSpec extends AnyFlatSpec with Matchers {
  "Schema derivation" should "work correctly" in {
    val person1 = Person("Gabriel", 45)
    val person2 = Person("Gabi", 54)

    val patch: Patch[Person]    = Schema[Person].diff(person1, person2)
    val inverted: Patch[Person] = patch.invert

    val result1: Either[String, Person] = patch.patch(person1)
    val result2: Either[String, Person] = result1.flatMap(inverted.patch)

    result2 shouldBe Right(person1)
  }
}
