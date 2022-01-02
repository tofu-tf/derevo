package derevo

import derevo.Derivation
import derevo.delegating
import derevo.Derevo
import _root_.sangria.schema._
import _root_.sangria.macros.derive._

package object sangria {

  type Ctx                             = Any
  type CtxObjectType[A]                = ObjectType[Ctx, A]
  type DeriveObjectSettingNoContext[A] = DeriveObjectSetting[Ctx, A]

  @delegating("sangria.macros.derive.deriveInputObjectType")
  object inputObjectType extends Derivation[InputObjectType] {

    def apply[A](arg: DeriveInputObjectSetting*): InputObjectType[A] =
      macro Derevo.delegateParamsV[InputObjectType, A, DeriveInputObjectSetting]

  }

  @delegating("sangria.macros.derive.deriveObjectType")
  object objectType extends Derivation[CtxObjectType] {

    def apply[A](arg: DeriveObjectSettingNoContext[A]*): CtxObjectType[A] =
      macro Derevo.delegateParamsV[CtxObjectType, A, DeriveObjectSettingNoContext[A]]
  }
}
