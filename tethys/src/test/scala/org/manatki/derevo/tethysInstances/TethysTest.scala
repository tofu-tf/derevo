package org.manatki.derevo.tethysInstances
import org.manatki.derevo.derive

import tethys._
import tethys.jackson._

@derive(tethysReader, tethysWriter)
final case class Foo(string: String, int: Int)


object TethysTest {
  def main(args: Array[String]): Unit = {
    println(Foo("ololo", 42).asJson)

    println(""" { "string": "kek", "int": -4 }" """.jsonAs[Foo])
  }

}
