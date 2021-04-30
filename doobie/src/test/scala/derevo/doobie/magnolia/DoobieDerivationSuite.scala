package derevo.doobie.magnolia

import derevo.derive
import derevo.doobie.magnolia.{doobieRead, doobieWrite}

object DoobieDerivationSpec {
  @derive(doobieRead, doobieWrite)
  final case class Foo(string: Option[String], int: Int)

  def main(args: Array[String]): Unit = {
    import doobie.syntax.string._

    println(sql"foobar".query[Foo])
  }
}