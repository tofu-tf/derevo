package derevo.tapir

import derevo.derive
import sttp.tapir._
import sttp.tapir.json.circe._

@derive(tapirSchema)
final case class Foo(string: Option[String], ints: List[Int], recursive: Foo)

object TSchemaTest {
  implicit val fooCodec: io.circe.Codec[Foo] = null

  def main(args: Array[String]): Unit = {
    println(endpoint.get.in(jsonBody[Foo]))
  }
}
