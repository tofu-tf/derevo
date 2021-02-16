package derevo.circe.magnolia

import derevo.{Derevo, Derivation, PolyDerivation, delegating}
import io.circe.magnolia.configured.Configuration
import io.circe.{Decoder, Encoder}
import derevo.NewTypeDerivation

@delegating("io.circe.magnolia.derivation.decoder.semiauto.deriveMagnoliaDecoder")
object decoder extends Derivation[Decoder] with NewTypeDerivation[Decoder] {
  def instance[A]: Decoder[A] = macro Derevo.delegate[Decoder, A]
}

@delegating("io.circe.magnolia.derivation.encoder.semiauto.deriveMagnoliaEncoder")
object encoder extends Derivation[Encoder] with NewTypeDerivation[Encoder] {
  def instance[A]: Encoder[A] = macro Derevo.delegate[Encoder, A]
}

@delegating("io.circe.magnolia.configured.decoder.semiauto.deriveConfiguredMagnoliaDecoder")
object customizableDecoder extends Derivation[Decoder] {
  def instance[A]: Decoder[A] = macro Derevo.delegate[Decoder, A]
}

@delegating("io.circe.magnolia.configured.encoder.semiauto.deriveConfiguredMagnoliaEncoder")
object customizableEncoder extends Derivation[Encoder] {
  def instance[A]: Encoder[A] = macro Derevo.delegate[Encoder, A]
}
