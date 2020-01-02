package derevo.pureconfig

import derevo.derive
import com.typesafe.config.ConfigFactory
import pureconfig.{ConfigReader, ConfigSource, ConfigWriter}
import pureconfig.syntax._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

@derive(pureconfigReader, pureconfigWriter)
case class Credentials(login: String, password: String)

@derive(pureconfigReader, pureconfigWriter)
case class Foo[A](m: A)

class DerivationSpec extends AnyFunSuite with Matchers {

  def roundTrip[A: ConfigReader: ConfigWriter](value: A): Unit = {
    val raw = value.toConfig.render
    ConfigSource.fromConfig(ConfigFactory.parseString(raw)).load[A] shouldBe Right(value)
  }

  test("writes and reads simple config") {
    roundTrip(Credentials("guest", "123"))
  }

  test("writes and reads config with parameter") {
    roundTrip(
      Foo(
        Map(
          "s1" -> Credentials("admin", "admin"),
          "s2" -> Credentials("test", "test")
        )
      )
    )
  }
}
