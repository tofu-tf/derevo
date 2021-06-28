package ciris

import com.typesafe.config.ConfigValueFactory
import com.typesafe.{config => typesafe}

import scala.reflect.runtime.universe.WeakTypeTag
import scala.util.Try
import scala.util.control.NonFatal

package object hocon {
  val HoconKeyType: ConfigKey = ConfigKey("HOCON")

  type ConfigValueDecoder[A] = ConfigDecoder[typesafe.ConfigValue, A]

  object instances extends ConfigValueDecoderInstances

  def hoconSource[Value](config: typesafe.Config, key: String)(implicit
      decoder: ConfigValueDecoder[Value]
  ): ConfigValue[Value] = {
    val configKey = ConfigKey(key)
    ConfigValue.suspend(readEntry(config, key, configKey) match {
      case Right(value) => ConfigValue.loaded(configKey, value).as[Value]
      case Left(value)  => ConfigValue.failed(value)
    })
  }

  def readEntry(
      config: typesafe.Config,
      key: String,
      configKey: ConfigKey
  ): Either[ConfigError, typesafe.ConfigValue] =
    Try(config.getValue(key)).fold(
      {
        case _: typesafe.ConfigException.Missing => Right(ConfigValueFactory.fromAnyRef(null))
        case NonFatal(ex)                        => Left(ConfigError(ex.getMessage))
      },
      Right(_)
    )

  private[hocon] val DummyPath         = "config-decoder-path"
  private[hocon] val SeqElementKeyType = ConfigKey("Seq element")
  private[hocon] val MapEntryKeyType   = ConfigKey("Map entry")

  private[hocon] def nonFatal[A: WeakTypeTag](
      fn: typesafe.Config => String => A
  ): ConfigValueDecoder[A] =
    catchNonFatal(cfg => path => Right(fn(cfg)(path)))

  private[hocon] def catchNonFatal[A](
      fn: typesafe.Config => String => Either[ConfigError, A]
  ): ConfigValueDecoder[A] =
    ConfigDecoder.lift(value =>
      Try(fn(value.atKey(DummyPath))(DummyPath)).fold(err => Left(ConfigError(err.getMessage)), identity)
    )
}
