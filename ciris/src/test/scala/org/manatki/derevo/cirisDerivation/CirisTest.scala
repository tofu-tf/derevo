package org.manatki.derevo.cirisDerivation

import org.manatki.derevo.derive
import scala.concurrent.duration.FiniteDuration

import ciris.loadConfig
import ciris.hocon.instances._
import ciris.hocon._
import com.typesafe.config.ConfigFactory

@derive(cirisDecoder)
case class Data(name: String, list: List[String], map: Map[String, Int], rate: Rate)

@derive(cirisDecoder)
case class Rate(elements: Int, duration: FiniteDuration)

object CirisTest {
  def main(args: Array[String]): Unit = {
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

    val res = loadConfig(hoconSource[Data](cfg).read("data")) { hocon => hocon }

    res.result.fold(
      e => e.messages.foreach(println),
      println(_)
    )
  }
}
