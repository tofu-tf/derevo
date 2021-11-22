package derevo

import io.estatico.newtype.macros.newtype

class DummyTC[A]

object DummyTC extends Derivation[DummyTC] with NewTypeDerivation[DummyTC] {
  def instance[A]: DummyTC[A] = new DummyTC
}

object Prefixed {
  val dummy = DummyTC
}

object DerivationSuite {
  implicit val dummyStr: DummyTC[String] = new DummyTC[String]

  @derive(DummyTC) @newtype
  case class Newtypoo(z: String)

  @derive(DummyTC) @newtype(unapply = true)
  case class Unapplypoo(z: String)

  @derive(DummyTC)
  case class Foo[A](x: A, y: List[A], z: String)

  @derive(DummyTC)
  case object Bar

  @derive(Prefixed.dummy)
  sealed trait Baz

  def checkNewtypoo        = implicitly[DummyTC[Newtypoo]]
  def checkUnapplypoo      = implicitly[DummyTC[Unapplypoo]]
  def checkFoo[A: DummyTC] = implicitly[DummyTC[Foo[A]]]
  def checkBar             = implicitly[DummyTC[Bar.type]]
  def checkBaz             = implicitly[DummyTC[Baz]]
}
