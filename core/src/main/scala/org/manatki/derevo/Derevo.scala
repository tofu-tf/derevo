package org.manatki.derevo

import scala.language.higherKinds
import scala.reflect.macros.blackbox

private trait Dummy1[X]

class Derevo(val c: blackbox.Context) {
  import c.universe._
  val DelegatingSymbol = typeOf[delegating].typeSymbol
  val PhantomSymbol    = typeOf[phantom].typeSymbol

  val DerivationSymbol = typeOf[Derivation[Dummy1]].typeConstructor.typeSymbol

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

  def instances(cls: ClassDef): List[Tree] =
    c.prefix.tree match {
      case q"new derive(..${instances})" =>
        instances
          .map(buildInstance(_, cls))
    }

  def buildInstance(tree: Tree, cls: ClassDef): Tree = {
    val typName = TypeName(cls.name.toString)

    val (name, tc, call) = tree match {
      case q"$obj(..$args)" =>
        val (name, typ) = nameAndType(obj)
        (name, typ, tree)

      case q"$obj.$method($args)" =>
        val (name, typ) = nameAndType(obj)
        (name, typ, tree)
      case q"$obj" =>
        val (name, typ) = nameAndType(obj)
        (name, typ, q"$obj.instance")
    }

    val tn      = TermName(name)
    val instTyp = tq"$tc[$typName]"
    val const   = tc.typeSymbol
    if (cls.tparams.isEmpty) q"implicit val $tn: $const[$typName] = $call"
    else {
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
          Some(q"val $name: $const[$typ]")
        }
      }
      val tps    = tparams.map(_.name)
      val appTyp = tq"$typName[..$tps]"
      q"implicit def $tn[..$tparams](implicit ..$implicits): $const[$appTyp] = $call"
    }
  }

  def nameAndType(obj: Tree): (String, Type) = {
    val name = obj match {
      case Ident(name) => c.freshName(name.toString)
    }

    val t = c.typecheck(obj).tpe.baseType(DerivationSymbol) match {
      case TypeRef(_, sym, List(inst)) => inst
      case _                           => abort(s"$obj seems not extending Derivation trait")
    }

    (name, t)
  }

  def debug(s: Any)    = c.info(c.enclosingPosition, s.toString, false)
  def abort(s: String) = c.abort(c.enclosingPosition, s)
}
