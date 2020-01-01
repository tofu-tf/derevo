package derevo.circe

import derevo.{Derevo, Derivation, PolyDerivation, delegating}
import io.circe.{Decoder, Encoder}

@delegating("io.circe.derivation.deriveDecoder")
object decoder extends Derivation[Decoder] {
  def instance[A]: Decoder[A] = macro Derevo.delegate[Decoder, A]
  io.circe.derivation.deriveDecoder
  /**
   * @param arg1 naming function. For example io.circe.derivation.renaming.snakeCase
   * @param arg2 useDefaults
   * @param arg3 type discriminator name
   */
  def apply[A](arg1: String => String, arg2: Boolean, arg3: Option[String]): Decoder[A] =
    macro Derevo.delegateParams3[Decoder, A, String => String, Boolean, Option[String]]
}

@delegating("io.circe.derivation.deriveEncoder")
object encoder extends PolyDerivation[Encoder, Encoder.AsObject] {
  def instance[A]: Encoder.AsObject[A] = macro Derevo.delegate[Encoder.AsObject, A]

  /**
   * @param arg1 naming function. For example io.circe.derivation.renaming.snakeCase
   * @param arg2 type discriminator name
   */
  def apply[A](arg1: String => String, arg2: Option[String]): Encoder.AsObject[A] =
    macro Derevo.delegateParams2[Encoder.AsObject, A, String => String, Option[String]]
}
