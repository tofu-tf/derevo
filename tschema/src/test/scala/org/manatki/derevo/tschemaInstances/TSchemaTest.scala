package org.manatki.derevo
package tschemaInstances

import io.circe.Printer
import ru.tinkoff.tschema.swagger._
import io.circe.syntax._

@derive(swagger)
final case class Foo(string: Option[String], ints: List[Int], recursive: Foo)

object TSchemaTest {
  implicit val printer = Printer.spaces4.copy(dropNullValues = true)
  def main(args: Array[String]): Unit = {
    println(SwaggerTypeable[Foo].typ.deref.value.asJson.pretty(printer))
  }
}
