package derevo.cats

import cats.Eq
import derevo.{Derivation, NewTypeDerivation}
import magnolia.{CaseClass, Magnolia, SealedTrait}

import scala.reflect.macros.blackbox

object eqv extends Derivation[Eq] with NewTypeDerivation[Eq] {
  type Typeclass[T] = Eq[T]

  def combine[T](ctx: CaseClass[Eq, T]): Eq[T] = new Eq[T] {
    override def eqv(x: T, y: T): Boolean =
      ctx.parameters.forall { p =>
        p.typeclass.eqv(p.dereference(x), p.dereference(y))
      }
  }

  def dispatch[T](ctx: SealedTrait[Eq, T]): Eq[T] =
    new Eq[T] {
      override def eqv(x: T, y: T): Boolean =
        ctx.dispatch(x) { sub =>
          sub.cast.isDefinedAt(y) && sub.typeclass.eqv(sub.cast(x), sub.cast(y))
        }
    }

  implicit def instance[T]: Eq[T] = macro Magnolia.gen[T]

  def apply[T](eqFields: String*): Eq[T] = macro eqExtendedImpl[T]

  def eqExtendedImpl[T: c.WeakTypeTag](c: blackbox.Context)(eqFields: c.Expr[String]*): c.Tree = {
    import c.universe._

    val T           = weakTypeOf[T]
    val eqFieldsSet = eqFields
      .map(_.tree)
      .collect { case Literal(Constant(field: String)) =>
        field
      }
      .toSet

    val comparisons = T.decls.collect {
      case m: MethodSymbol if m.isCaseAccessor && eqFieldsSet.contains(m.name.toString) =>
        val name = m.name.toTermName
        q"Eq[${m.typeSignature}].eqv(x.$name, y.$name)"
    }

    q"""
        new Eq[$T] {
          def eqv(x: $T, y: $T): Boolean = {
            ..$comparisons
          }
        }
      """
  }

  object universal extends Derivation[Eq] {
    implicit def instance[T]: Eq[T] = Eq.fromUniversalEquals[T]
  }
}
