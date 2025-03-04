package derevo.cats

import cats.Eq
import derevo.cats._
import derevo.derive
import org.scalatest.freespec.AnyFreeSpec
import io.estatico.newtype.macros.newtype

class EqSpec extends AnyFreeSpec {

  "Cats Eq derivation" - {
    "should derive eq instances" - {
      "through magnolia" in {
        import cats.instances.int._

        @derive(eqv)
        case class Qux(a: Int)

        assert(Eq[Qux].eqv(Qux(1), Qux(1)))
        assert(Eq[Qux].neqv(Qux(1), Qux(-1)))
      }

      "through newtype casting" in {
        import derevo.cats.EqSpec.Jankurpo

        assert(Eq[Jankurpo].eqv(Jankurpo("a"), Jankurpo("a")))
        assert(Eq[Jankurpo].neqv(Jankurpo("a"), Jankurpo("b")))
      }

      "through Eq.fromUniversalEquals" in {
        @derive(eqv.universal)
        case class Qux(a: Int)

        assert(Eq[Qux].eqv(Qux(1), Qux(1)))
        assert(Eq[Qux].neqv(Qux(1), Qux(-1)))
      }

      "with ignoring fields" in {
        @derive(eqv("bar"))
        case class Foo(bar: Int, baz: Int)

        assert(Eq[Foo].eqv(Foo(1, 2), Foo(1, 5)))
        assert(Eq[Foo].neqv(Foo(2, 2), Foo(3, 2)))
        assert(Eq[Foo].neqv(Foo(2, 2), Foo(3, 1)))
      }
    }
  }
}

object EqSpec {
  @derive(eqv)
  @newtype
  case class Jankurpo(a: String)
}
