import scala.annotation.StaticAnnotation

package derevo {
  class derive(instances: Any*) extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro Derevo.deriveMacro
  }

  class delegating(to: String, args: Any*) extends StaticAnnotation
  class phantom extends StaticAnnotation

  sealed trait InstanceDef
  trait Derivation[TC[_]]                  extends InstanceDef
  trait DerivationK1[TC[_[_]]]             extends InstanceDef
  trait DerivationK2[TC[_[_[_]]]]          extends InstanceDef
  trait PolyDerivation[FromTC[_], ToTC[_]] extends InstanceDef
}

package object derevo {
  type InjectInstancesHere >: Null
  def insertInstancesHere(): InjectInstancesHere = null
}
