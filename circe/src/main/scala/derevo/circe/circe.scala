package derevo.circe

import derevo.{Derevo, Derivation, PolyDerivation, delegating}
import io.circe.{Decoder, Encoder}

@delegating("io.circe.derivation.deriveDecoder")
object decoder extends Derivation[Decoder] {
  def instance[A]: Decoder[A] = macro Derevo.delegate[Decoder, A]

  def apply[A](arg1: String => String, arg2: Boolean, arg3: Option[String]): Decoder[A] =
    macro Derevo.delegateParams3[Decoder, A, String => String, Boolean, Option[String]]
}

@delegating("io.circe.derivation.deriveEncoder")
object encoder extends PolyDerivation[Encoder, Encoder.AsObject] {
  def instance[A]: Encoder.AsObject[A] = macro Derevo.delegate[Encoder.AsObject, A]

  def apply[A](arg1: String => String, arg2: Option[String]): Encoder.AsObject[A] =
    macro Derevo.delegateParams2[Encoder.AsObject, A, String => String, Option[String]]
}
