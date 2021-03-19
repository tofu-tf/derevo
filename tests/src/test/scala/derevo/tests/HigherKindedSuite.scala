package derevo
package tests

import scala.languageFeature.experimental.macros
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._
import io.circe.syntax._

import org.scalatest.funsuite.AnyFunSuite
import io.circe.Json

@derive(Jampa)
trait Goo[G[_]] {
  def goo = "goo"
}

@derive(Jampa)
trait Zee[A, F[_]] {
  def zee = "zee"
}

@derive(Cody)
case class Nee[F[_]](greeting: F[String])

@derive(Cody)
case class San[A, F[_]](first: A, second: F[A])

class HigherKindedMacroSuite extends AnyFunSuite {
  test("goo should be goo") {
    assert(implicitly[Goo[Jampa]].goo === "goo")
  }

  test("zee should be zee") {
    assert(implicitly[Zee[String, Jampa]].zee === "zee")
  }

  test("nee can json") {
    assert(Nee[Option](Some("hello")).asJson == Json.obj("greeting" -> "hello".asJson))
  }

  test("san can json") {
    assert(
      San[String, Option]("tadaima", Some("okaeri")).asJson == Json
        .obj("first" -> "tadaima".asJson, "second" -> "okaeri".asJson)
    )
  }
}
