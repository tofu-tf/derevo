package derevo.ciris

import _root_.ciris.hocon.{ConfigValueDecoder, instances, readEntry}
import _root_.ciris.{ConfigDecoder, ConfigError, ConfigKey}
import ciris.ConfigError.Missing
import com.typesafe.config.{ConfigObject, ConfigOrigin, ConfigValueFactory}
import com.typesafe.{config => typesafe}
import magnolia.{CaseClass, Magnolia, SealedTrait}
import derevo.Derivation

import scala.language.experimental.macros
import derevo.NewTypeDerivation

object cirisDecoder extends Derivation[ConfigValueDecoder] with NewTypeDerivation[ConfigValueDecoder] {
  type Typeclass[T]  = ConfigValueDecoder[T]
  type ErrorOrParams = Either[ConfigError, List[Any]]

  def combine[T](ctx: CaseClass[Typeclass, T]): Typeclass[T] = ConfigDecoder
    .lift[typesafe.ConfigValue, T] {
      case config: ConfigObject =>
        val cfg = (key: String) => readEntry(config.toConfig, key, originKeyType(config.origin))

        val errorOrParams = instances.collectErrors(
          ctx.parameters
            .map(param => cfg(param.label).flatMap(param.typeclass.decode(Some(ConfigKey(param.label)), _)))
            .toList
        )

        errorOrParams.map(params => ctx.rawConstruct(params.reverse))
      case other                =>
        Left(
          ConfigError(
            s"Cannot derive for ${other.render()}"
          )
        )
    }

  private[ciris] def originKeyType(origin: ConfigOrigin): ConfigKey =
    ConfigKey(s"HOCON from ${origin.description}")

  def dispatch[T](ctx: SealedTrait[Typeclass, T]): Typeclass[T] = ???

  implicit def instance[T]: Typeclass[T] = macro Magnolia.gen[T]
}
