package org.manatki.derevo

import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter}

package object reactivemongoDerivation {
  @delegating("reactivemongo.bson.Macros.writer")
  object bsonDocumentWriter extends Derivation[BSONDocumentWriter] {
    def instance[A]: BSONDocumentWriter[A] = macro Derevo.delegate[BSONDocumentWriter, A]
  }

  @delegating("reactivemongo.bson.Macros.reader")
  object bsonDocumentReader extends Derivation[BSONDocumentReader] {
    def instance[A]: BSONDocumentReader[A] = macro Derevo.delegate[BSONDocumentReader, A]
  }
}
