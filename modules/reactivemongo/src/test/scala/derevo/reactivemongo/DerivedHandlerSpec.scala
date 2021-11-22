package derevo.reactivemongo

import derevo.derive
import io.estatico.newtype.macros.newtype
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.refspec.RefSpec
import reactivemongo.bson.{BSONReader, BSONInteger, BSONDocumentWriter, document, BSONString, BSONDocumentReader}

@derive(bsonDocumentWriter, bsonDocumentReader)
case class Peka(yoba: String, amount: Int)

@derive(bsonDocumentWriter, bsonDocumentReader)
case class Pekarnya[T](yoba: String, peka: T)

object newtypeStuff {
  @derive(bsonNewtypeWriter, bsonNewtypeReader)
  @newtype
  case class Newtypo(str: String)

  @derive(bsonDocumentWriter, bsonDocumentReader)
  case class NewtypoContainer(newtypo: Newtypo)
}

import newtypeStuff._

class DerivedHandlerSpec extends RefSpec with Matchers {
  object `Derived writer` {
    def `should serialize`: Assertion = {
      implicitly[BSONDocumentWriter[Peka]].write(Peka("azaza", 42)) shouldBe document(
        "yoba"   -> BSONString("azaza"),
        "amount" -> BSONInteger(42)
      )
    }

    def `should serialize newtype`: Assertion = {
      implicitly[BsonValueWriter[Newtypo]].write(Newtypo("azaza")) shouldBe BSONString("azaza")
    }

    def `should serialize newtype container`: Assertion = {
      implicitly[BSONDocumentWriter[NewtypoContainer]].write(NewtypoContainer(Newtypo("azaza"))) shouldBe document(
        "newtypo" -> BSONString("azaza"),
      )
    }

    def `should serialize polymorphic classes` = {
      implicitly[BSONDocumentWriter[Pekarnya[Peka]]].write(Pekarnya("peka", Peka("azaza", 42))) shouldBe document(
        "yoba" -> BSONString("peka"),
        "peka" -> document(
          "yoba"   -> BSONString("azaza"),
          "amount" -> BSONInteger(42)
        )
      )
    }
  }

  object `Deriver reader` {
    def `should deserialize`: Assertion = {
      implicitly[BSONDocumentReader[Peka]].read(
        document(
          "yoba"   -> BSONString("azaza"),
          "amount" -> BSONInteger(42)
        )
      ) shouldBe Peka("azaza", 42)
    }

    def `should deserialize newtype`: Assertion = {
      implicitly[BsonValueReader[Newtypo]]
        .asInstanceOf[BSONReader[BSONString, Newtypo]] // hack to avoid covariance hell
        .read(BSONString("azaza")) shouldBe Newtypo("azaza")
    }

    def `should deserialize newtype container`: Assertion = {
      implicitly[BSONDocumentReader[NewtypoContainer]].read(
        document(
          "newtypo" -> BSONString("azaza"),
        )
      ) shouldBe NewtypoContainer(Newtypo("azaza"))
    }

    def `should deserialize polymorphic classes`: Assertion = {
      implicitly[BSONDocumentReader[Pekarnya[Peka]]].read(
        document(
          "yoba" -> BSONString("peka"),
          "peka" -> document(
            "yoba"   -> BSONString("azaza"),
            "amount" -> BSONInteger(42)
          )
        )
      ) shouldBe Pekarnya("peka", Peka("azaza", 42))
    }
  }
}
