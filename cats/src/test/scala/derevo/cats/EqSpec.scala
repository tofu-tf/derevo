package derevo.cats

import cats.Eq
import derevo.cats.{eq => eqv}
import derevo.derive
import org.scalatest.freespec.AnyFreeSpec

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

      "through Eq.fromUniversalEquals" in {
        @derive(eqv.universal)
        case class Qux(a: Int)

        assert(Eq[Qux].eqv(Qux(1), Qux(1)))
        assert(Eq[Qux].neqv(Qux(1), Qux(-1)))
      }
    }
  }
}
