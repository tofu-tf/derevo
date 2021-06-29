package derevo.tests

import derevo._

object LocalImportTest {
  import derevo.circe.codec

  object a {}
  import a._

  @derive(codec) case class Foo()
}
