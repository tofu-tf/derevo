package org.manatki.derevo

import scala.annotation.{StaticAnnotation, compileTimeOnly}

@compileTimeOnly("enable macro paradise to expand macro annotations")
class derive(instances: Quoted*) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro DerevoMacro.deriveMacro
}
