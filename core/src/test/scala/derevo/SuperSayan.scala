package derevo

import supertagged.TaggedType
import scala.reflect.{ClassTag, classTag}
import org.scalatest.flatspec.AnyFlatSpec

class Monkasu[_](val cls: Any)

object Monkasu extends Derivation[Monkasu] {
  def newtype[R]: Newtype[R] = new Newtype

  class Newtype[R] {
    def instance[A](implicit c: ClassTag[R]): Monkasu[A] = new Monkasu(c)
  }
}

object SuperSayan {
  @derive(Monkasu)
  object Goku extends TaggedType[String]
  type Goku = Goku.Type
}

class SuperSayan extends AnyFlatSpec {
  "derived supertagged type" should "be String" in {
    assert(implicitly[Monkasu[SuperSayan.Goku]].cls === classTag[String])
  }
}
