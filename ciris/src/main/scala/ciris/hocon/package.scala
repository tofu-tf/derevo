package ciris

import scala.util.control.NonFatal

import ciris.api.Id
import com.typesafe.{config => typesafe}

package object hocon {
  val HoconKeyType: ConfigKeyType[String] = ConfigKeyType("HOCON")

  object instances extends ConfigValueDecoderInstances

  def hoconSource[Value](config: typesafe.Config)(
      implicit decoder: ConfigDecoder[typesafe.ConfigValue, Value]
  ): ConfigSource[Id, String, Value] =
    ConfigSource(HoconKeyType) { path =>
      readEntry(config, HoconKeyType, path, path)
        .decodeValue[Value]
        .value
    }

  def readEntry(
      config: typesafe.Config,
      keyType: ConfigKeyType[String],
      key: String,
      path: String
  ): ConfigEntry[Id, String, typesafe.ConfigValue, typesafe.ConfigValue] =
    ConfigEntry(path, keyType, try Right(config.getValue(key)) catch {
      case _: typesafe.ConfigException.Missing => Left(ConfigError.missingKey(path, keyType))
      case NonFatal(ex)                        => Left(ConfigError.readException(path, keyType, ex))
    })
}
