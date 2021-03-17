import scala.languageFeature.experimental.macros
import scala.reflect.ClassTag
import scala.reflect.macros.blackbox

import derevo.DerivationKN3
import derevo.PassTypeArgs
import derevo.derive
import org.scalatest.funsuite.AnyFunSuite

@derive(Jampa)
trait Goo[G[_]] {
  def goo = "goo"
}

trait Jampa[_]

object JJ {
  type Of[U[f[_]]] = U[Jampa]

  def jampa[U[f[_]]](c: blackbox.Context)(implicit t: c.WeakTypeTag[U[Any]]): c.Tree = {
    import c.universe._
    val u     = t.tpe.typeConstructor.typeSymbol
    val jampa = typeOf[Jampa[Any]].typeConstructor
    q"new $u[$jampa]{}"
  }
}

object Jampa extends DerivationKN3[JJ.Of] with PassTypeArgs {
  def instance[U[f[_]]]: U[Jampa] = macro JJ.jampa[U]
}

class HigherKindedMacroSuite extends AnyFunSuite {
  test("goo should be goo") {
    assert(implicitly[Goo[Jampa]].goo === "goo")
  }
}
