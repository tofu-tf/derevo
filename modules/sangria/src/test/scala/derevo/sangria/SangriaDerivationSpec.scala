package derevo.sangria

import derevo.derive
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sangria.schema.InputObjectType
import sangria.macros.derive.InputObjectTypeName
import sangria.macros.derive.ExcludeInputFields
class SangriaDerivationSpec extends AnyFlatSpec with Matchers {
  "Sangria input object derivation" should "derive input object without customization" in {
    val derived: InputObjectType[SangriaDerivationSpec.Foo] = implicitly[InputObjectType[SangriaDerivationSpec.Foo]]

    assertResult("Foo")(derived.name)
    derived.fields.map(_.name) should contain theSameElementsAs List("stringParam", "intParam")
  }

  it should "derive input object with customization" in {
    val derived: InputObjectType[SangriaDerivationSpec.Foo2] = implicitly[InputObjectType[SangriaDerivationSpec.Foo2]]

    assertResult("Renamed")(derived.name)
    derived.fields.map(_.name) should contain theSameElementsAs List("stringParam", "fooInner")
  }
}

object SangriaDerivationSpec {
  @derive(inputObjectType()) case class Foo(stringParam: String, intParam: Int)
  @derive(inputObjectType(InputObjectTypeName("Renamed"), ExcludeInputFields("intParam"))) case class Foo2(
      stringParam: String,
      intParam: Int,
      fooInner: Foo
  )
}
