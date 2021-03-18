package derevo
package tests

import scala.languageFeature.experimental.macros
import scala.reflect.ClassTag

import org.scalatest.funsuite.AnyFunSuite

@derive(Jampa)
trait Goo[G[_]] {
  def goo = "goo"
}

class HigherKindedMacroSuite extends AnyFunSuite {
  test("goo should be goo") {
    assert(implicitly[Goo[Jampa]].goo === "goo")
  }
}
