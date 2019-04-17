package org.manatki

import scala.annotation.{StaticAnnotation, compileTimeOnly}

package derevo {

  @compileTimeOnly("enable macro paradise to expand macro annotations")
  class derive(instances: InstanceDef*) extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro Derevo.deriveMacro
  }

  class delegating(to: String) extends StaticAnnotation
  class phantom

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
