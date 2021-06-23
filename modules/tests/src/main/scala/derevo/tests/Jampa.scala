package derevo
package tests

import scala.reflect.macros.blackbox
import io.circe.Encoder

import scala.annotation.nowarn

trait Jampa[_]

object JJ {
  type Of[U[_[_]]]         = U[Jampa]
  type EncoderOpt[U[_[_]]] = Encoder[U[Option]]
}

/** this is basic macro derivation method
  * unfortunately then calling a macro having higher-kinded type parameter
  * scala refuses to infer the type and Derevo should pass argument to it explicitly
  * this is where the trait PassTypeArgs is helpful
  */
object Jampa extends DerivationKN3[JJ.Of] with PassTypeArgs {
  def instance[U[f[_]]]: U[Jampa] = macro jampa[U]

  def jampa[U[f[_]]](c: blackbox.Context)(implicit t: c.WeakTypeTag[U[Any]]): c.Tree = {
    import c.universe._
    @nowarn val jampa = typeOf[Jampa[Any]].typeConstructor

    val u = t.tpe match {
      case PolyType(_, tr) =>
        tr match {
          case TypeRef(t, s, args) => appliedType(s, args.init :+ jampa)
        }
      case _               => appliedType(t.tpe, jampa)

    }

    q"new $u{}"
  }
}

/** this simple derivation would fail
  * if type U would have unbounded type parameters in its definition
  * so it's will fail for any type that has more than one parameter
  * unless each parameter will have Encoder bound on it
  */
@delegating("io.circe.derivation.deriveEncoder")
object Cody extends DerivationKN3[JJ.EncoderOpt] with ParamRequire[Encoder] with PassTypeArgs {
  def instance[U[f[_]]]: Encoder[U[Option]] = macro Derevo.delegate[Encoder.AsObject, U[Option]]
}
