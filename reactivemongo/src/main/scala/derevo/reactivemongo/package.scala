package derevo

import _root_.reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, BSONReader, BSONValue, BSONWriter}

package object reactivemongo {
  type BsonValueReader[A] = BSONReader[_ <: BSONValue, A]

  type BsonValueWriter[A] = BSONWriter[A, _ <: BSONValue]

  @delegating("reactivemongo.bson.Macros.writer")
  object bsonDocumentWriter extends PolyDerivation[BsonValueWriter, BSONDocumentWriter] {
    def instance[A]: BSONDocumentWriter[A] = macro Derevo.delegate[BSONDocumentWriter, A]
  }

  @delegating("reactivemongo.bson.Macros.reader")
  object bsonDocumentReader extends PolyDerivation[BsonValueReader, BSONDocumentReader] {
    def instance[A]: BSONDocumentReader[A] = macro Derevo.delegate[BSONDocumentReader, A]
  }
}
