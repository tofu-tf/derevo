package org.manatki.derevo.circeDerivation
import io.circe.{Decoder, Encoder}
import org.manatki.derevo.{Derevo, Derivation, PolyDerivation, delegating}

@delegating("io.circe.derivation.deriveDecoder")
object decoder extends Derivation[Decoder] {
  def instance[A]: Decoder[A] = macro Derevo.delegate[Decoder, A]

  def apply[A](arg: String => String): Decoder[A] = macro Derevo.delegateParam[Decoder, A, String => String]
}

@delegating("io.circe.derivation.deriveEncoder")
object encoder extends PolyDerivation[Encoder, Encoder.AsObject] {
  def instance[A]: Encoder.AsObject[A] = macro Derevo.delegate[Encoder.AsObject, A]

  def apply[A](arg: String => String): Encoder.AsObject[A] = macro Derevo.delegateParam[Encoder.AsObject, A, String => String]
}
