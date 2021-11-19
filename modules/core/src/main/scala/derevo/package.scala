import scala.annotation.StaticAnnotation

package derevo {
  class derive(instances: Any*) extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro Derevo.deriveMacro
  }

  class composite(instances: Any*) extends StaticAnnotation

  /** */
  trait PassTypeArgs
  trait KeepRefinements

  class delegating(to: String, args: Any*) extends StaticAnnotation
  class phantom                            extends StaticAnnotation

  trait ParamRequire[TC[_]]

  // * numeration according to https://docs.tofu.tf/docs/internal/kind-enumeration
  sealed trait InstanceDef
  trait Derivation[TC[_]]                             extends InstanceDef
  trait DerivationKN1[TC[f[_]]]                       extends InstanceDef
  trait DerivationKN2[TC[bf[_, _]]]                   extends InstanceDef
  trait DerivationKN3[TC[alg[f[_]]]]                  extends InstanceDef
  trait DerivationKN4[TC[tr[f[_], _]]]                extends InstanceDef
  trait DerivationKN5[TC[tf[_, _, _]]]                extends InstanceDef
  trait DerivationKN11[TC[alg[bf[_, _]]]]             extends InstanceDef
  trait DerivationKN17[TC[alg[btr[_, _], _, _]]]      extends InstanceDef
  trait SpecificDerivation[FromTC[_], ToTC[_], NT[_]] extends InstanceDef

  class CompositeDerivation extends InstanceDef

}

package object derevo {
  type InjectInstancesHere >: Null
  def insertInstancesHere(): InjectInstancesHere = null

  type DerivationK1[TC[f[_]]]               = DerivationKN1[TC]
  type DerivationBi1[TC[f[_, _]]]           = DerivationKN2[TC]
  type DerivationTri1[TC[f[_, _, _]]]       = DerivationKN5[TC]
  type DerivationK2[TC[alg[f[_]]]]          = DerivationKN3[TC]
  type DerivationHK[TC[alg[f[_]]]]          = DerivationKN3[TC]
  type DerivationBi2[TC[alf[bf[_, _]]]]     = DerivationKN11[TC]
  type DerivationTr[TC[T[f[_], a]]]         = DerivationKN4[TC]
  type DerivationBiTr[TC[T[f[_, _], a, b]]] = DerivationKN17[TC]
  type PolyDerivation[FromTC[_], ToTC[_]]   = SpecificDerivation[FromTC, ToTC, ToTC]
}
