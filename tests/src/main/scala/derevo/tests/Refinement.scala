package derevo.tests

import derevo._

trait Trait[T]

object refinedTrait extends Derivation[Trait] with KeepRefinements {
  def apply[T, A](a: A): Trait[T] { type Refinement = A } = new Trait[T] { type Refinement = A }
}

object Test {
  @derive(refinedTrait("123")) case class Foo()
  @derive(refinedTrait(123)) case class PolymorphicFoo[@phantom Arg](arg: Arg)

  val nonRefined: Trait[Foo] = implicitly
  val refined: Trait[Foo] { type Refinement = String } = implicitly
  val refinedPolymorphic: Trait[PolymorphicFoo[Foo]] { type Refinement = Int } = implicitly
}