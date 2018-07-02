package org.manatki.derevo.circeDerivation
import io.circe.{Decoder, ObjectEncoder}
import org.manatki.derevo.{Derevo, Derivation, delegating}

@delegating("io.circe.derivation.deriveDecoder")
object decoder extends Derivation[Decoder]{
  def instance[A]: Decoder[A] = macro Derevo.delegate[Decoder, A]

  def apply[A](arg: String => String) = macro Derevo.delegateParam[Decoder, A, String => String]
}

@delegating("io.circe.derivation.deriveEncoder")
object encoder extends Derivation[ObjectEncoder]{
  def instance[A]: ObjectEncoder[A] = macro Derevo.delegate[ObjectEncoder, A]

  def apply[A](arg: String => String) = macro Derevo.delegateParam[ObjectEncoder, A, String => String]
}

