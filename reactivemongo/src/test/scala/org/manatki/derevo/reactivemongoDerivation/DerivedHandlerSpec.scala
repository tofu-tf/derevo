package org.manatki.derevo.reactivemongoDerivation

import org.manatki.derevo.derive
import org.scalatest.Matchers
import org.scalatest.refspec.RefSpec
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, BSONInteger, BSONString, document}

@derive(bsonDocumentWriter, bsonDocumentReader)
case class Peka(yoba: String, amount: Int)

class DerivedHandlerSpec extends RefSpec with Matchers {
  object `Derived writer` {
    def `should serialize`: Unit = {
      implicitly[BSONDocumentWriter[Peka]].write(Peka("azaza", 42)) shouldBe document(
        "yoba" -> BSONString("azaza"),
        "amount" -> BSONInteger(42)
      )
    }
  }

  object `Deriver reader` {
    def `should deserialize`: Unit = {
      implicitly[BSONDocumentReader[Peka]].read(
        document(
          "yoba" -> BSONString("azaza"),
          "amount" -> BSONInteger(42)
        )
      ) shouldBe Peka("azaza", 42)
    }
  }
}
