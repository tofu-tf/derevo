package derevo.tapir

import derevo.Derivation
import magnolia.Magnolia
import sttp.tapir.Schema
import sttp.tapir.generic.internal.SchemaMagnoliaDerivation

import scala.language.experimental.macros

object tapirSchema extends Derivation[Schema] with SchemaMagnoliaDerivation {
  def instance[A]: Schema[A] = macro Magnolia.gen[A]
}
