package derevo.cats

import cats.Semigroup
import magnolia.{CaseClass, Magnolia, SealedTrait}
import derevo.Derivation

object semigroup extends Derivation[Semigroup] {
  type Typeclass[T] = Semigroup[T]

  def combine[T](ctx: CaseClass[Semigroup, T]): Semigroup[T] = new Semigroup[T] {
    override def combine(x: T, y: T): T =
      ctx.construct(param => param.typeclass.combine(param.dereference(x), param.dereference(y)))
  }

  def dispatch[T](ctx: SealedTrait[Semigroup, T]): Semigroup[T] = ???
  implicit def instance[T]: Semigroup[T] = macro Magnolia.gen[T]
}
