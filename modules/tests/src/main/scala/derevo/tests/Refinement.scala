package derevo.tests

import derevo._

trait Trait[T]

object refinedTrait extends Derivation[Trait] with KeepRefinements {
  def apply[T, A](a: A): Trait[T] { type Refinement = A } = new Trait[T] { type Refinement = A }
  def singleTparam[T](dummy: Int): Trait[T] { type Refinement = String } = new Trait[T] { type Refinement = String }
}

object refinedTraitWithInstanceMethod extends Derivation[Trait] with KeepRefinements {
  def instance[T]: Trait[T] { type Refinement = String } = new Trait[T] { type Refinement = String }
}

object Test {
  @derive(refinedTrait("123")) case class Foo()
  @derive(refinedTrait.singleTparam(123)) case class Bar()
  @derive(refinedTraitWithInstanceMethod) case class Baz()
  @derive(refinedTrait(123)) case class PolymorphicFoo[@phantom Arg](arg: Arg)

  val nonRefined: Trait[Foo] = implicitly
  val refinedFoo: Trait[Foo] { type Refinement = String } = implicitly
  val refinedBar: Trait[Bar] { type Refinement = String } = implicitly
  val refinedBaz: Trait[Baz] { type Refinement = String } = implicitly
  val refinedPolymorphic: Trait[PolymorphicFoo[Foo]] { type Refinement = Int } = implicitly
}
