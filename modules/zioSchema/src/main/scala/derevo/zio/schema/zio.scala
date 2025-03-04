package derevo.zio.schema

import derevo.{Derevo, Derivation, NewTypeDerivation, delegating}
import zio.schema.Schema

@delegating("zio.schema.DeriveSchema.gen")
object schema extends Derivation[Schema] with NewTypeDerivation[Schema] {
  def instance[A]: Schema[A] = macro Derevo.delegate[Schema, A]
}
