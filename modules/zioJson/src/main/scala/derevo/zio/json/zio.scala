package derevo.zio.json

import derevo.{Derevo, Derivation, NewTypeDerivation, delegating}
import zio.json._

@delegating("zio.json.DeriveJsonDecoder.gen")
object jsonDecoder extends Derivation[JsonDecoder] with NewTypeDerivation[JsonDecoder] {
  def instance[A]: JsonDecoder[A] = macro Derevo.delegate[JsonDecoder, A]
}

@delegating("zio.json.DeriveJsonEncoder.gen")
object jsonEncoder extends Derivation[JsonEncoder] with NewTypeDerivation[JsonEncoder] {
  def instance[A]: JsonEncoder[A] = macro Derevo.delegate[JsonEncoder, A]
}
