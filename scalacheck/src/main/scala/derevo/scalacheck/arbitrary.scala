package derevo.scalacheck

import derevo.Derivation
import magnolia.{CaseClass, Magnolia, Param, SealedTrait}
import mercator.Monadic
import org.scalacheck.{Arbitrary, Gen}
import arbitraryInstances._

object arbitrary extends Derivation[Arbitrary] {
  type Typeclass[T] = Arbitrary[T]

  def combine[T](ctx: CaseClass[Arbitrary, T]): Arbitrary[T] =
    ctx.constructMonadic(
      param =>
        Arbitrary(
          param.typeclass.arbitrary.map(x => x: Param[Arbitrary, T]#PType)
        )
    )

  def dispatch[T](ctx: SealedTrait[Arbitrary, T]): Arbitrary[T] = {
    val randomElement = ctx.subtypes.toList match {
      case Nil => throw new Exception(s"No direct subtypes of ${ctx.typeName}")
      case nel => Gen.choose(0, nel.size - 1).map(nel.apply)
    }

    Arbitrary(randomElement.flatMap(s => s.typeclass.arbitrary))
  }

  implicit def instance[T]: Arbitrary[T] = macro Magnolia.gen[T]
}

object arbitraryInstances {
  implicit val monadicInstance: Monadic[Arbitrary] = new Monadic[Arbitrary] {
    override def point[A](value: A): Arbitrary[A] =
      Arbitrary(Gen.const(value))

    override def flatMap[A, B](from: Arbitrary[A])(fn: A => Arbitrary[B]): Arbitrary[B] =
      Arbitrary(from.arbitrary.flatMap(x => fn(x).arbitrary))

    override def map[A, B](from: Arbitrary[A])(fn: A => B): Arbitrary[B] =
      Arbitrary(from.arbitrary.map(fn))
  }
}
