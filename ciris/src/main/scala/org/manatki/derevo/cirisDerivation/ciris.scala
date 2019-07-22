package org.manatki.derevo.cirisDerivation

import _root_.ciris.api.Monad
import _root_.ciris.hocon.{ConfigValueDecoder, readEntry}
import _root_.ciris.{ConfigEntry, ConfigError, ConfigKeyType}
import com.typesafe.config.{ConfigObject, ConfigOrigin, ConfigValue}
import magnolia.{CaseClass, Magnolia, SealedTrait}
import org.manatki.derevo.Derivation

import scala.language.experimental.macros

object cirisDecoder extends Derivation[ConfigValueDecoder] {
  type Typeclass[T]  = ConfigValueDecoder[T]
  type ErrorOrParams = Either[ConfigError, List[Any]]

  def combine[T](ctx: CaseClass[Typeclass, T]): Typeclass[T] = new ConfigValueDecoder[T] {
    def decode[F[_], K, S](entry: ConfigEntry[F, K, S, ConfigValue])(implicit F: Monad[F]): F[Either[ConfigError, T]] =
      entry.flatMapValue {
        case config: ConfigObject =>
          val cfg              = (key: String) => readEntry(config.toConfig, originKeyType(config.origin), key, s"${entry.key}.$key")
          var params           = new Array[Any](ctx.parameters.size)
          var err: ConfigError = null

          var i = 0
          while (i < ctx.parameters.size) {
            val param   = ctx.parameters(i)
            val decoded = param.typeclass.decode(cfg(param.label)).asInstanceOf[Either[ConfigError, param.PType]]

            if (err == null) {
              decoded match {
                case Right(value) => params(i) = value
                case Left(e) =>
                  params = null
                  err = e
              }
            } else {
              decoded match {
                case Left(e) => err = err combine e
                case _       =>
              }
            }

            i += 1
          }

          if (err != null) Left(err)
          else Right(ctx.rawConstruct(params))

          val errorOrParams = ctx.parameters.foldLeft[ErrorOrParams](Right(Nil)) { (acc, param) =>
            val decoded = param.typeclass.decode(cfg(param.label)).asInstanceOf[Either[ConfigError, param.PType]]

            (acc, decoded) match {
              case (Right(list), Right(value)) => Right(value :: list)
              case (Left(errA), Left(errB))    => Left(errA combine errB)
              case (err @ Left(_), Right(_))   => err
              case (Right(_), err @ Left(_))   => err.asInstanceOf[ErrorOrParams]
            }
          }
          errorOrParams.right.map(params => ctx.rawConstruct(params.reverse))
        case other =>
          Left(
            ConfigError.wrongType(
              entry.key.toString,
              originKeyType(other.origin),
              Right(other),
              other,
              ctx.typeName.full,
              None
            ))
      }.value

  }

  private[cirisDerivation] def originKeyType(origin: ConfigOrigin): ConfigKeyType[String] =
    ConfigKeyType(s"HOCON from ${origin.description}")

  def dispatch[T](ctx: SealedTrait[Typeclass, T]): Typeclass[T] = ???

  implicit def instance[T]: Typeclass[T] = macro Magnolia.gen[T]
}
