package org.manatki.derevo
import scala.reflect.macros.blackbox

class DerevoMacro(val c: blackbox.Context) {
  import c.universe._

  def delegateMacro[TC[_], I](expr: c.Expr[TC[I]]): c.Expr[TC[I]] = expr

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

  def instances(cls: ClassDef): List[Tree] = {
    val values = c.prefix.tree match {
      case q"new derive(..${instances})" =>
        instances
          .map(buildInstance)
    }

    List(q"""override def toString = "Hello, " + ${cls.name.toString} + " "  + ${values.toString}""")
  }


  def buildInstance(tree: Tree): Tree = {
    val (name, typ, call) = tree match {
      case q"$obj.$method(..$args)" =>
        val (name, typ) = nameAndType(obj)
        (name, typ, tree)
      case q"$obj" =>
        val (name, typ) = nameAndType(obj)
        (name, typ, q"$obj.apply()")
    }

    val tn = TermName(name)
    q"val $tn: $typ = $call"
  }

  def nameAndType(obj: Tree): (String, Type) = {
    debug(obj.symbol)
    debug(obj.isTerm)
    val name = obj match {
      case Ident(name) => name.toString
    }
    val x = obj.tpe
    (name, x)
  }

  def debug(s: Any) = c.info(c.enclosingPosition, s.toString, false)
}

