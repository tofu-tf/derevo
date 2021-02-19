package derevo.tethys

import derevo.{Derevo, Derivation, delegating}
import tethys.derivation.builder._
import tethys.{JsonObjectWriter, JsonReader, JsonWriter}
import derevo.NewTypeDerivation
import derevo.SpecificDerivation

@delegating("tethys.derivation.semiauto.jsonReader")
object tethysReader extends Derivation[JsonReader] with NewTypeDerivation[JsonReader] {
  def instance[A]: JsonReader[A] = macro Derevo.delegate[JsonReader, A]

  def apply[A](arg: ReaderDerivationConfig): JsonReader[A] =
    macro Derevo.delegateParam[JsonReader, A, ReaderDerivationConfig]
}

@delegating("tethys.derivation.semiauto.jsonWriter")
object tethysWriter
    extends SpecificDerivation[JsonWriter, JsonObjectWriter, JsonWriter] with NewTypeDerivation[JsonWriter] {
  def instance[A]: JsonObjectWriter[A] = macro Derevo.delegate[JsonObjectWriter, A]

  def apply[A](arg: WriterDerivationConfig): JsonObjectWriter[A] =
    macro Derevo.delegateParam[JsonObjectWriter, A, WriterDerivationConfig]
}
