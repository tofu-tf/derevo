package org.manatki.derevo.tethysInstances
import org.manatki.derevo.{Derevo, Derivation, delegating}
import tethys.{JsonObjectWriter, JsonReader, JsonWriter}

@delegating("tethys.derivation.semiauto.jsonReader")
object tethysReader extends Derivation[JsonReader]{
  def instance[A]: JsonReader[A] = macro Derevo.delegate[JsonReader, A]
}

@delegating("tethys.derivation.semiauto.jsonWriter")
object tethysWriter extends Derivation[JsonObjectWriter]{
  def instance[A]: JsonObjectWriter[A] = macro Derevo.delegate[JsonObjectWriter, A]
}
