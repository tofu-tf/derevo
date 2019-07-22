package org.manatki.derevo.cirisDerivation

import ciris.hocon._
import ciris.refined._
import ciris.loadConfig
import com.typesafe.config.ConfigFactory
import eu.timepit.refined.string.Url
import org.manatki.derevo.derive
import eu.timepit.refined.api.Refined
import eu.timepit.refined.types.net.PortNumber
import scala.concurrent.duration.FiniteDuration

@derive(cirisDecoder)
case class Rate(elements: Int, duration: FiniteDuration)

@derive(cirisDecoder)
case class Data(name: String, host: String Refined Url, port: PortNumber, list: List[Int], map: Map[String, Int], rate: Rate)

object CirisTest {
  def main(args: Array[String]): Unit = {
    val cfg = ConfigFactory.parseString(
      """
        |data {
        |  name = Demo
        |  host = "https://www.site.ru"
        |  port = 100
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

    val res = loadConfig(hoconSource[ciris.api.Id, Data](cfg).read("data")) { hocon =>
      hocon
    }

    res.result.fold(
      e => e.messages.foreach(println),
      a=>  a.host
    )
  }
}
