package derevo.circe

import derevo.{Derevo, Derivation, delegating}
import io.circe.derivation.renaming
import io.circe.{Codec, Decoder, Encoder}
import derevo.NewTypeDerivation
import derevo.SpecificDerivation

@delegating("io.circe.derivation.deriveDecoder")
object decoder extends Derivation[Decoder] with NewTypeDerivation[Decoder] {
  def instance[A]: Decoder[A] = macro Derevo.delegate[Decoder, A]

  /** @param arg1
    *   naming function. For example io.circe.derivation.renaming.snakeCase
    * @param arg2
    *   useDefaults
    * @param arg3
    *   type discriminator name
    */
  def apply[A](arg1: String => String, arg2: Boolean, arg3: Option[String]): Decoder[A] =
    macro Derevo.delegateParams3[Decoder, A, String => String, Boolean, Option[String]]
}

@delegating("io.circe.derivation.deriveEncoder")
object encoder extends SpecificDerivation[Encoder, Encoder.AsObject, Encoder] with NewTypeDerivation[Encoder] {
  def instance[A]: Encoder.AsObject[A] = macro Derevo.delegate[Encoder.AsObject, A]

  /** @param arg1
    *   naming function. For example io.circe.derivation.renaming.snakeCase
    * @param arg2
    *   type discriminator name
    */
  def apply[A](arg1: String => String, arg2: Option[String]): Encoder.AsObject[A] =
    macro Derevo.delegateParams2[Encoder.AsObject, A, String => String, Option[String]]
}

@delegating("io.circe.derivation.deriveCodec")
object codec extends SpecificDerivation[Codec, Codec.AsObject, Codec] with NewTypeDerivation[Codec] {
  def instance[A]: Codec.AsObject[A] = macro Derevo.delegate[Codec.AsObject, A]

  /** @param arg1
    *   naming function. For example io.circe.derivation.renaming.snakeCase
    * @param arg2
    *   type discriminator name
    */
  def apply[A](arg1: String => String, arg2: Boolean, arg3: Option[String]): Codec.AsObject[A] =
    macro Derevo.delegateParams3[Codec.AsObject, A, String => String, Boolean, Option[String]]
}

@delegating("io.circe.derivation.deriveDecoder", renaming.snakeCase, false, None)
object snakeDecoder extends Derivation[Decoder] {
  def instance[A]: Decoder[A] = macro Derevo.delegate[Decoder, A]
}

@delegating("io.circe.derivation.deriveEncoder", renaming.snakeCase, None)
object snakeEncoder extends SpecificDerivation[Encoder, Encoder.AsObject, Encoder] {
  def instance[A]: Encoder.AsObject[A] = macro Derevo.delegate[Encoder.AsObject, A]
}

@delegating("io.circe.derivation.deriveCodec", renaming.snakeCase, false, None)
object snakeCodec extends SpecificDerivation[Codec, Codec.AsObject, Encoder] {
  def instance[A]: Codec.AsObject[A] = macro Derevo.delegate[Codec.AsObject, A]
}

@delegating("io.circe.derivation.deriveDecoder", renaming.kebabCase, false, None)
object kebabDecoder extends Derivation[Decoder] {
  def instance[A]: Decoder[A] = macro Derevo.delegate[Decoder, A]
}

@delegating("io.circe.derivation.deriveEncoder", renaming.kebabCase, None)
object kebabEncoder extends SpecificDerivation[Encoder, Encoder.AsObject, Encoder] {
  def instance[A]: Encoder.AsObject[A] = macro Derevo.delegate[Encoder.AsObject, A]
}

@delegating("io.circe.derivation.deriveCodec", renaming.kebabCase, false, None)
object kebabCodec extends SpecificDerivation[Codec, Codec.AsObject, Encoder] {
  def instance[A]: Codec.AsObject[A] = macro Derevo.delegate[Codec.AsObject, A]
}
