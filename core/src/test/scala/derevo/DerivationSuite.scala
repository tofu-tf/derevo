package derevo

class DummyTC[A]

object DummyTC extends Derivation[DummyTC] {
  def instance[A]: DummyTC[A] = new DummyTC
}

object DerivationSuite {
  @derive(DummyTC)
  case class Foo[A](x: A, y: List[A], z: String)

  @derive(DummyTC)
  case object Bar

  def checkFoo[A: DummyTC] = implicitly[DummyTC[Foo[A]]]
  def checkBar             = implicitly[DummyTC[Bar.type]]
}
