package derevo
package tests

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

trait Jampa[_]

object JJ {
  type Of[U[_[_]]] = U[Jampa]
}

object Jampa extends DerivationKN3[JJ.Of] with PassTypeArgs {
  def instance[U[f[_]]]: U[Jampa] = macro jampa[U]

  def jampa[U[f[_]]](c: blackbox.Context)(implicit t: c.WeakTypeTag[U[Any]]): c.Tree = {
    import c.universe._
    val u     = t.tpe.typeConstructor.typeSymbol
    val jampa = typeOf[Jampa[Any]].typeConstructor
    q"new $u[$jampa]{}"
  }
}
