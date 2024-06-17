package derevo.vulcan

import derevo.{Derivation, NewTypeDerivation}
import magnolia.{CaseClass, Magnolia, SealedTrait}
import vulcan.Codec
import vulcan.generic._

object avroCodec extends Derivation[Codec] with NewTypeDerivation[Codec] {

  type Typeclass[A] = Codec[A]

  def combine[A](caseClass: CaseClass[Codec, A]): Codec[A] = Codec.combine(caseClass)

  def dispatch[A](sealedTrait: SealedTrait[Codec, A]): Codec.Aux[Any, A] = Codec.dispatch(sealedTrait)

  def instance[A]: Codec[A] = macro Magnolia.gen[A]

}
