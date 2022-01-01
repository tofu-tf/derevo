package derevo.sangria

import sangria.schema.InputObjectType
import derevo.Derivation
import derevo.delegating
import derevo.Derevo
import sangria.macros.derive.DeriveInputObjectSetting

@delegating("sangria.macros.derive.deriveInputObjectType")
object inputObjectType extends Derivation[InputObjectType] {

  def apply[A](arg: DeriveInputObjectSetting*): InputObjectType[A] =
    macro Derevo.delegateParamsV[InputObjectType, A, DeriveInputObjectSetting]

}
