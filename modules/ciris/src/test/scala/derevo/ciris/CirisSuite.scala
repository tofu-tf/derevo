package derevo.ciris

import derevo.derive
import com.typesafe.config.ConfigFactory
import cats.effect.IO
import ciris.hocon._
import ciris.hocon.instances._
import scala.concurrent.duration.Duration

import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.ExecutionContext

class CirisSuite extends AnyFunSuite {

  implicit val contextShift = IO.contextShift(ExecutionContext.global)

  test("hoconSource example") {

    @derive(cirisDecoder)
    case class Data(name: String, list: List[String], map: Map[String, Int], rate: Rate)

    @derive(cirisDecoder)
    case class Rate(elements: Int, duration: Option[Duration])

    val cfg = ConfigFactory.parseString(
      """
        |data {
        |  name = Demo
        |  list = [1, 2, 3]
        |  map.wtf = 1
        |  map.lol = 2
        |  map.wut = 3
        |  rate {
        |    elements = 2
        |    duration = 100 millis
        |  }
        |}
      """.stripMargin
    )

    val res = hoconSource[Data](cfg, "data").load[IO].unsafeRunSync()

    assert(
      res == Data(
        "Demo",
        List("1", "2", "3"),
        Map("wtf" -> 1, "lol" -> 2, "wut" -> 3),
        Rate(2, Option(Duration(100, "millis")))
      )
    )
  }

  test("README.md example") {

    @derive(cirisDecoder)
    case class DataConfig(name: String, addresses: List[String], mapping: Map[String, Int])

    val cfg = ConfigFactory.parseString(
      """
        |data {
        |  name = AAA
        |  addresses = [home, work, pub]
        |  mapping.until = 1
        |  mapping.from  = 2
        |  mapping.to    = 3
        |}
      """.stripMargin
    )

    val dataConfig = hoconSource[DataConfig](cfg, "data").as[DataConfig].load[IO].unsafeRunSync()

    assert(dataConfig == DataConfig("AAA", List("home", "work", "pub"), Map("until" -> 1, "from" -> 2, "to" -> 3)))
  }

}
