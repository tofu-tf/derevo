package derevo.cats

import cats.Monoid
import magnolia.{CaseClass, Magnolia, SealedTrait}
import derevo.Derivation
import scala.annotation.implicitNotFound
import derevo.NewTypeDerivation

object monoid extends Derivation[Monoid] with NewTypeDerivation[Monoid] {
  type Typeclass[T] = Monoid[T]

  def combine[T](ctx: CaseClass[Monoid, T]): Monoid[T] = new Monoid[T] {
    override def combine(x: T, y: T): T =
      ctx.construct(param => param.typeclass.combine(param.dereference(x), param.dereference(y)))
    override def empty: T               = ctx.construct(_.typeclass.empty)
  }

  def dispatch[T](ctx: SealedTrait[Monoid, T])(implicit absurd: MonoidSumAbsurd): Monoid[T] = absurd.mon

  implicit def instance[T]: Monoid[T] = macro Magnolia.gen[T]
}

@implicitNotFound("Can not derive Monoids for sealed families")
abstract class MonoidSumAbsurd {
  def mon[A]: Monoid[A]
}
