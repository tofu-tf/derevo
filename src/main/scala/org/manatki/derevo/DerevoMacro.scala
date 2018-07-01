package org.manatki.derevo
import scala.reflect.macros.blackbox

class DerevoMacro(val c: blackbox.Context) {
  import c.universe._

  def instanceMacro[I: c.WeakTypeTag](name: c.Expr[String])(expr: c.Expr[I]): c.Expr[Quoted] = {
    val q"${s: String}" = name

    c.Expr[Quoted](q"__root__.org.manatki.derevo.Quoted($s, $expr)")
  }

  def deriveMacro(annottees: Tree*): Tree =
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

  def instances(cls: ClassDef): Vector[Tree] =
    Vector(q"""override def toString = "Hello, " + ${cls.name.toString} """)
}
