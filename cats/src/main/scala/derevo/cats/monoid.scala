package derevo.cats

import cats.Monoid
import magnolia.{CaseClass, Magnolia, SealedTrait}
import derevo.Derivation

object monoid extends Derivation[Monoid] {
  type Typeclass[T] = Monoid[T]

  def combine[T](ctx: CaseClass[Monoid, T]): Monoid[T] = new Monoid[T] {
    override def combine(x: T, y: T): T =
      ctx.construct(param => param.typeclass.combine(param.dereference(x), param.dereference(y)))
    override def empty: T = ctx.construct(_.typeclass.empty)
  }

  def dispatch[T](ctx: SealedTrait[Monoid, T]): Monoid[T] = ???

  implicit def instance[T]: Monoid[T] = macro Magnolia.gen[T]
}
