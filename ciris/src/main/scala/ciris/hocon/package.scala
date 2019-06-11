package ciris

import scala.util.control.NonFatal
import ciris.api.{Applicative, Monad}
import com.typesafe.{config => typesafe}

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
}
