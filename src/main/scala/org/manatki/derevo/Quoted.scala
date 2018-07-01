package org.manatki.derevo
import scala.reflect.macros.blackbox

final case class Quoted(name: String, expr: Any)

trait QuoteFunctions {
  def instance[TC[_]](name: String)(expr: TC[This]): Quoted =
    macro DerevoMacro.instanceMacro[TC[This]]
}
