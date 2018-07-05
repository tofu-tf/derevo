package org.manatki.derevo

import scala.language.higherKinds
import scala.reflect.macros.blackbox

private trait Dummy1[X]

class Derevo(val c: blackbox.Context) {
  import c.universe._
  import internal.typeRef
  val DelegatingSymbol = typeOf[delegating].typeSymbol
  val PhantomSymbol    = typeOf[phantom].typeSymbol

  val DerivationSymbol         = typeOf[Derivation[Dummy1]].typeConstructor.typeSymbol
  val SpecificDerivationSymbol = typeOf[PolyDerivation[Dummy1, Dummy1]].typeConstructor.typeSymbol

  def delegate[TC[_], I]: c.Expr[TC[I]] =
    c.Expr(delegation(c.prefix.tree, None, false))

  def delegateParam[TC[_], I, Arg](arg: c.Expr[Arg]): c.Expr[TC[I]] =
    c.Expr(delegation(c.prefix.tree, Some(arg), true))

  def delegateParams[TC[_], I, Args](args: c.Expr[Args]): c.Expr[TC[I]] =
    c.Expr(delegation(c.prefix.tree, Some(args), false))

  private def delegation[Args](tree: Tree, maybeArg: Option[c.Expr[Args]], single: Boolean): Tree = {
    val annots = tree.tpe.termSymbol.annotations
    val s = annots
      .map(_.tree)
      .collectFirst {
        case q"new $cls(${to: Tree})" if cls.symbol == DelegatingSymbol =>
          c.eval(c.Expr[String](to))
      }
      .getOrElse(abort(s"could not find @delegating annotation at $tree"))

    val call = s.split("\\.").map(TermName(_)).foldLeft[Tree](q"_root_")((a, b) => q"$a.$b")

    maybeArg.fold(call) {
      case arg if single => q"$call($arg)"
      case q"(..$args)"  => q"$call(..$args)"
    }
  }

  def deriveMacro(annottees: Tree*): Tree = {
    annottees match {
      case Seq(cls: ClassDef) =>
        q"""
           $cls
           object ${cls.name.toTermName} {
               ..${instances(cls)}
           }
         """

      case Seq(
          cls: ClassDef,
          q"object $companion extends {..$earlyDefs} with ..$parents{$self => ..$defs}"
          ) =>
        q"""
           $cls
           object $companion extends {..$earlyDefs} with ..$parents{$self =>
             ..$defs
             ..${instances(cls)}
           }
         """
    }
  }

  private def instances(cls: ClassDef): List[Tree] =
    c.prefix.tree match {
      case q"new derive(..${instances})" =>
        instances
          .map(buildInstance(_, cls))
    }

  private def buildInstance(tree: Tree, cls: ClassDef): Tree = {
    val typName = TypeName(cls.name.toString)

    val (name, fromTc, toTc, call) = tree match {
      case q"$obj(..$args)" =>
        val (name, from, to) = nameAndTypes(obj)
        (name, from, to, tree)

      case q"$obj.$method($args)" =>
        val (name, from, to) = nameAndTypes(obj)
        (name, from, to, tree)
      case q"$obj" =>
        val (name, from, to) = nameAndTypes(obj)
        (name, from, to, q"$obj.instance")
    }

    val tn        = TermName(name)
    if (cls.tparams.isEmpty) {
      val resT = mkAppliedType(toTc, tq"$typName")
      q"implicit val $tn: $resT = $call"
    } else {
      val tparams = cls.tparams
      val implicits = tparams.flatMap { tparam =>
        val phantom =
          tparam.mods.annotations.exists { t =>
            c.typecheck(t).tpe.typeSymbol == PhantomSymbol
          }
        if (phantom) None
        else {
          val name = c.freshName[TermName]("ev")
          val typ  = tparam.name
          val reqT = mkAppliedType(fromTc, tq"$typ")
          Some(q"val $name: $reqT")
        }
      }
      val tps    = tparams.map(_.name)
      val appTyp = tq"$typName[..$tps]"
      val resT   = mkAppliedType(toTc, appTyp)
      q"implicit def $tn[..$tparams](implicit ..$implicits): $resT = $call"
    }
  }

  private def mkAppliedType(tc: Type, arg: Tree): Tree = tc match {
    case TypeRef(pre, sym, ps) => tq"$sym[..$ps, $arg]"
    case _                     => tq"$tc[$arg]"
  }

  private def nameAndTypes(obj: Tree): (String, Type, Type) = {
    val name = obj match {
      case Ident(name) => c.freshName(name.toString)
    }

    val (from, to) = c.typecheck(obj).tpe.baseType(DerivationSymbol) match {
      case TypeRef(_, sym, List(tc)) => (tc, tc)
      case _ =>
        c.typecheck(obj).tpe.baseType(SpecificDerivationSymbol) match {
          case TypeRef(_, sym, List(from, to)) => (from, to)
          case _ =>
            abort(s"$obj seems not extending Derivation or SpecificDerivation traits")
        }

    }

    (name, from, to)
  }

  private def debug(s: Any)    = c.info(c.enclosingPosition, s.toString, false)
  private def abort(s: String) = c.abort(c.enclosingPosition, s)
}
