package org.manatki.derevo.catsInstances

import cats.Order
import magnolia.{CaseClass, Magnolia, SealedTrait}
import org.manatki.derevo.Derivation

object order extends Derivation[Order] {
  type Typeclass[T] = Order[T]

  def combine[T](ctx: CaseClass[Order, T]): Order[T] = new Order[T] {
    override def compare(x: T, y: T): Int =
      ctx.parameters.view
        .map(p => p.typeclass.compare(p.dereference(x), p.dereference(y)))
        .find(_ != 0)
        .getOrElse(0)
    override def eqv(x: T, y: T): Boolean =
      ctx.parameters.forall { p =>
        p.typeclass.eqv(p.dereference(x), p.dereference(y))
      }
  }

  def dispatch[T](ctx: SealedTrait[Order, T]): Order[T] =
    new Order[T] {

      override def compare(x: T, y: T): Int =
        ctx.dispatch(x) { subX =>
          ctx.dispatch(y) { subY =>
            if (subX eq subY)
              subX.typeclass.compare(subX.cast(x), subX.cast(y))
            else ctx.subtypes.indexOf(subX) - ctx.subtypes.indexOf(subY)
          }
        }

      override def eqv(x: T, y: T): Boolean =
        ctx.dispatch(x) { sub =>
          sub.cast.isDefinedAt(y) && sub.typeclass.eqv(sub.cast(x), sub.cast(y))
        }
    }

  implicit def instance[T]: Order[T] = macro Magnolia.gen[T]
}
