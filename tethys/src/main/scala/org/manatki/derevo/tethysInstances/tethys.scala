package org.manatki.derevo.tethysInstances
import org.manatki.derevo.{Derevo, Derivation, PolyDerivation, delegating}
import tethys.derivation.builder._
import tethys.{JsonObjectWriter, JsonReader, JsonWriter}

@delegating("tethys.derivation.semiauto.jsonReader")
object tethysReader extends Derivation[JsonReader] {
  def instance[A]: JsonReader[A] = macro Derevo.delegate[JsonReader, A]

  def apply[A](arg: ReaderDerivationConfig): JsonReader[A] =
    macro Derevo.delegateParam[JsonReader, A, ReaderDerivationConfig]
}

@delegating("tethys.derivation.semiauto.jsonWriter")
object tethysWriter extends PolyDerivation[JsonWriter, JsonObjectWriter] {
  def instance[A]: JsonObjectWriter[A] = macro Derevo.delegate[JsonObjectWriter, A]

  def apply[A](arg: WriterDerivationConfig): JsonObjectWriter[A] =
    macro Derevo.delegateParam[JsonObjectWriter, A, WriterDerivationConfig]
}
