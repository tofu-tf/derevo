package derevo.pureconfig

import derevo.derive
import com.typesafe.config.ConfigFactory
import org.scalatest.Assertion
import pureconfig.{ConfigReader, ConfigSource, ConfigWriter}
import pureconfig.syntax._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

@derive(config, configWriter)
case class CredentialsM(login: String, password: String)

@derive(config, configWriter)
case class FooM[A](m: A)

class DerivationSpecMagnolia extends AnyFunSuite with Matchers {

  def roundTrip[A: ConfigReader: ConfigWriter](value: A): Assertion = {
    val raw = value.toConfig.render
    ConfigSource.fromConfig(ConfigFactory.parseString(raw)).load[A] shouldBe Right(value)
  }

  test("writes and reads simple config") {
    roundTrip(CredentialsM("guest", "123"))
  }

  test("writes and reads config with parameter") {
    roundTrip(
      FooM(
        Map(
          "s1" -> CredentialsM("admin", "admin"),
          "s2" -> CredentialsM("test", "test")
        )
      )
    )
  }
}
