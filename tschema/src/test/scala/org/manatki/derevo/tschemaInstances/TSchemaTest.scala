package org.manatki.derevo
package tschemaInstances

import io.circe.Printer
import ru.tinkoff.tschema.swagger._
import io.circe.syntax._
import ru.tinkoff.tschema.param.{HttpMultiParam, HttpParam}

@derive(swagger)
final case class Foo(string: Option[String], ints: List[Int], recursive: Foo)

@derive(openapiParam, httpParam)
final case class Bar(name: Option[String], age: Long)

object TSchemaTest {
  implicit val printer = Printer.spaces4.copy(dropNullValues = true)
  def main(args: Array[String]): Unit = {
    println(SwaggerTypeable[Foo].typ.deref.value.asJson.pretty(printer))

    implicitly[HttpParam[Bar]] match {
      case bm: HttpMultiParam[Bar] =>
        println(bm.names)
        println(bm.applyOpt(List(None, Some("2"))))
      case _ =>
    }

    implicitly[AsOpenApiParam[Bar]] match {
      case bm: AsMultiOpenApiParam[Bar] =>
        println(bm.fields)
      case _ =>
    }
  }


}
