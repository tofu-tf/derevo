package ciris

import ciris.ConfigError.wrongType
import ciris.api.syntax._
import ciris.api.{Applicative, Monad}
import com.typesafe.{config => typesafe}

import scala.reflect.runtime.universe.{WeakTypeTag, weakTypeOf}
import scala.util.control.NonFatal

package object hocon {
  val HoconKeyType: ConfigKeyType[String] = ConfigKeyType("HOCON")

  object instances extends ConfigValueDecoderInstances

  def hoconSource[F[_]: Monad, Value](config: typesafe.Config)(
      implicit decoder: ConfigDecoder[typesafe.ConfigValue, Value]
  ): ConfigSource[F, String, Value] =
    ConfigSource.applyF(HoconKeyType) { path =>
      readEntry[F](config, HoconKeyType, path, path)
        .decodeValue[Value]
        .value
    }

  def readEntry[F[_]: Applicative](
      config: typesafe.Config,
      keyType: ConfigKeyType[String],
      key: String,
      path: String
  ): ConfigEntry[F, String, typesafe.ConfigValue, typesafe.ConfigValue] =
    ConfigEntry.applyF(
      path,
      keyType,
      Applicative[F].pure(
        try Right(config.getValue(key))
        catch {
          case _: typesafe.ConfigException.Missing => Left(ConfigError.missingKey(path, keyType))
          case NonFatal(ex)                        => Left(ConfigError.readException(path, keyType, ex))
        })
    )

  private[hocon] val DummyPath         = "config-decoder-path"
  private[hocon] val SeqElementKeyType = ConfigKeyType[Int]("Seq element")
  private[hocon] val MapEntryKeyType   = ConfigKeyType[String]("Map entry")

  private[hocon] def catchNonFatal[A: WeakTypeTag](fn: typesafe.Config => String => A): ConfigValueDecoder[A] =
    new ConfigValueDecoder[A] {
      def decode[F[_]: Monad, K, S](entry: ConfigEntry[F, K, S, typesafe.ConfigValue]): F[Either[ConfigError, A]] = {
        for {
          sourceValue  <- entry.sourceValue
          errorOrValue <- entry.value
        } yield
          errorOrValue.right.flatMap { value =>
            try Right(fn(value.atKey(DummyPath))(DummyPath))
            catch {
              case NonFatal(cause) =>
                Left(wrongType(entry.key, entry.keyType, sourceValue, value, weakTypeOf[A].toString, Some(cause)))
            }
          }
      }
    }
}
