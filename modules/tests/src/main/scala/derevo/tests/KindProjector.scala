package derevo.tests

import derevo._

trait TwoArgsTrait[A, B]

object kpTrait extends Derivation[TwoArgsTrait[String, *]] {
  def instance[T]: TwoArgsTrait[String, T] = null
}

object KindProjectorTest {
  @derive(kpTrait) case class Foo()

  val impl = implicitly[TwoArgsTrait[String, Foo]]
}
