package derevo

import io.estatico.newtype.macros.newtype
import scala.language.implicitConversions
import scala.reflect.{ClassTag, classTag}
import org.scalatest.flatspec.AnyFlatSpec

class Kyon[A](val cls: Any)

object Kyon extends Derivation[Kyon] {
  def instance[A](implicit cls: ClassTag[A]): Kyon[A] = new Kyon(cls)
  def newtype[Repr]: Newtype[Repr]                    = new Newtype[Repr]

  class Newtype[Repr] {
    def instance[A](implicit cls: ClassTag[Repr]): Kyon[A] = new Kyon(cls)
  }
}

class KyonSuite extends AnyFlatSpec {
  import KyonSuite._

  "newtype derivation" should "implicitly receive classtag" in {
    assert(implicitly[Kyon[Datus[Unit]]].cls === classTag[String])
  }
}

object KyonSuite {
  @derive(Kyon)
  @newtype
  case class Datus[@phantom A](x: String)
}
