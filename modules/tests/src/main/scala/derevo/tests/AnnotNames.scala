package derevo.tests

import derevo.{derive, Derivation}
import derevo.{derive => derive2}

object sample extends Derivation[List] {
  def instance[T]: List[T] = Nil
}

object AnnotNamesTest {
  @derive(sample) case class Foo()
  @derevo.derive(sample)
  case class Bar()
  @ _root_.derevo.derive(sample)
  case class Baz()
  @derive2(sample) case class Qux()

  val foo = implicitly[List[Foo]]
  val bar = implicitly[List[Bar]]
  val baz = implicitly[List[Baz]]
  val qux = implicitly[List[Qux]]
}
