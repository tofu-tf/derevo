package derevo.ciris

import derevo.derive
import com.typesafe.config.ConfigFactory
import cats.effect.IO
import ciris.hocon._
import ciris.hocon.instances._

import scala.concurrent.ExecutionContext
@derive(cirisDecoder)
case class Data(name: String, list: List[String], map: Map[String, Int], rate: Rate)

@derive(cirisDecoder)
case class Rate(elements: Int)

object CirisSpec {
  def main(args: Array[String]): Unit = {
    implicit val contextShift = IO.contextShift(ExecutionContext.global)

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
    println(hoconSource[Data](cfg, "data").load[IO].unsafeRunSync())
  }
}
