package derevo.circe.magnolia

import derevo.{Derevo, Derivation, delegating, NewTypeDerivation}
import io.circe.{Decoder, Encoder, KeyDecoder, KeyEncoder}
import magnolia.{CaseClass, Magnolia, SealedTrait}

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

object keyDecoder extends Derivation[KeyDecoder] with NewTypeDerivation[KeyDecoder] {
  type Typeclass[T] = KeyDecoder[T]

  def combine[T](ctx: CaseClass[KeyDecoder, T]): KeyDecoder[T] = new KeyDecoder[T] {
    def apply(key: String): Option[T] = {
      val parts = key.split("::")
      if (parts.length != ctx.parameters.length) None
      else ctx.constructMonadic(p => p.typeclass.apply(parts(p.index)))
    }
  }

  def instance[T]: KeyDecoder[T] = macro Magnolia.gen[T]
}

private[circe] class keyEncoder(sep: String = "::") {
  type Typeclass[T] = KeyEncoder[T]

  def combine[T](ctx: CaseClass[KeyEncoder, T]): KeyEncoder[T] =
    if (ctx.isObject) _ => ctx.typeName.short
    else { cc =>
      ctx.parameters.view.map(p => p.typeclass(p.dereference(cc))).mkString(sep)
    }

  def dispatch[T](ctx: SealedTrait[KeyEncoder, T]): KeyEncoder[T] =
    obj => ctx.dispatch(obj)(sub => sub.typeclass(sub.cast(obj)))

  def instance[T]: KeyEncoder[T] = macro Magnolia.gen[T]
}

object keyEncoder extends keyEncoder("::") with Derivation[KeyEncoder] with NewTypeDerivation[KeyEncoder]
