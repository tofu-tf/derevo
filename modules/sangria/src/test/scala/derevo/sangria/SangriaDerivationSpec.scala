package derevo.sangria

import derevo.derive
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sangria.schema.InputObjectType
import sangria.macros.derive.InputObjectTypeName
import sangria.macros.derive.ExcludeInputFields
import sangria.macros.derive.ObjectTypeName
import sangria.macros.derive.RenameField

class SangriaDerivationSpec extends AnyFlatSpec with Matchers {
  "Sangria input object derivation" should "derive input object without customization" in {
    val derived = implicitly[InputObjectType[SangriaDerivationSpec.Input]]

    assertResult("Input")(derived.name)
    derived.fields.map(_.name) should contain theSameElementsAs List("stringParam", "intParam")
  }

  it should "derive input object with customization" in {

    val derived =
      implicitly[InputObjectType[SangriaDerivationSpec.InputCustom]]

    assertResult("Renamed")(derived.name)
    derived.fields.map(_.name) should contain theSameElementsAs List("stringParam", "fooInner")
  }

  "Sangria object derivation" should "derive object without customization" in {
    val derived =
      implicitly[CtxObjectType[SangriaDerivationSpec.Response]]

    assertResult("Response")(derived.name)
    println(derived)
    derived.fields.map(_.name) should contain theSameElementsAs List("data")
  }

  "Sangria object derivation" should "derive object with customization" in {
    val derived =
      implicitly[CtxObjectType[SangriaDerivationSpec.ResponseCustom]]

    assertResult("RenamedResponse")(derived.name)
    println(derived)
    derived.fields.map(_.name) should contain theSameElementsAs List("content")
  }
}

object SangriaDerivationSpec {
  @derive(inputObjectType()) case class Input(stringParam: String, intParam: Int)
  @derive(inputObjectType(InputObjectTypeName("Renamed"), ExcludeInputFields("intParam"))) case class InputCustom(
      stringParam: String,
      intParam: Int,
      fooInner: Input
  )

  @derive(objectType())
  case class Response(data: String)

  @derive(objectType(ObjectTypeName("RenamedResponse"), RenameField("data", "content")))
  case class ResponseCustom(data: String)
}
