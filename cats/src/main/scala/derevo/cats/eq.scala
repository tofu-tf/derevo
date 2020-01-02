package derevo.cats

import cats.Eq
import magnolia.{CaseClass, Magnolia, SealedTrait}
import derevo.Derivation

object eq extends Derivation[Eq] {
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
}
