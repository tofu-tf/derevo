package org.manatki.derevo.cirisDerivation

import scala.language.experimental.macros
import scala.language.reflectiveCalls

import _root_.ciris.api.Monad
import _root_.ciris.hocon.readEntry
import _root_.ciris.{ConfigDecoder, ConfigEntry, ConfigError, ConfigKeyType}
import com.typesafe.config.{ConfigObject, ConfigOrigin, ConfigValue}
import magnolia.{CaseClass, Magnolia, SealedTrait}
import org.manatki.derevo.Derivation

object cirisDecoder extends Derivation[ConfigValueDecoder] {
  type Typeclass[T] = ConfigDecoder[ConfigValue, T]

  def combine[T](ctx: CaseClass[Typeclass, T]): Typeclass[T] = new ConfigDecoder[ConfigValue, T] {
    def decode[F[_], K, S](entry: ConfigEntry[F, K, S, ConfigValue])(implicit F: Monad[F]): F[Either[ConfigError, T]] =
      entry
        .flatMapValue {
          case config: ConfigObject =>
            val cfg = (key: String) => readEntry(config.toConfig, originKeyType(config.origin), key, s"${entry.key}.$key")
            val params = ctx.parameters.foldLeft[Either[ConfigError, Map[String, Any]]](Right(Map.empty)) { (acc, param) =>
              val value = param.typeclass.decode(cfg(param.label)).map((param.label, _))

              (acc, value) match {
                case (Right(map), Right(pair))      => Right(map + pair)
                case (Left(prevErr), Left(nextErr)) => Left(prevErr.combine(nextErr))
                case (err@Left(_), Right(_))        => err
                case (Right(_), err@Left(_))        => err.asInstanceOf[Either[ConfigError, Map[String, Any]]]
              }
            }

            params.map(pMap => ctx.construct { p => pMap(p.label) })
          case other =>
            Left(ConfigError.wrongType(
              entry.key.toString,
              originKeyType(other.origin),
              Right(other),
              other,
              ctx.typeName.full,
              None
            ))
        }
        .value

    private[this] def originKeyType(origin: ConfigOrigin): ConfigKeyType[String] =
      ConfigKeyType(s"HOCON from ${origin.description}")
  }

  def dispatch[T](ctx: SealedTrait[Typeclass, T]): Typeclass[T] = ???

  implicit def instance[T]: Typeclass[T] = macro Magnolia.gen[T]
}