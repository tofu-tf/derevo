package ciris.hocon

import scala.collection.generic.CanBuildFrom
import scala.concurrent.duration.{Duration, FiniteDuration, NANOSECONDS}

import ciris.api.Monad
import ciris.{ConfigDecoder, ConfigEntry, ConfigError, ConfigKeyType}
import com.typesafe.config.{ConfigException, ConfigMemorySize, ConfigUtil, ConfigValue}

trait ConfigValueDecoderInstances {
  private[this] val DummyPath = "config-decoder-path"
  private[this] val SeqElementKeyType = ConfigKeyType[String]("Seq element")
  private[this] val MapEntryKeyType = ConfigKeyType[String]("Map entry")

  implicit val stringConfigDecoder: ConfigDecoder[ConfigValue, String] =
    ConfigDecoder.catchNonFatal("String") { value =>
      value.atKey(DummyPath).getString(DummyPath)
    }

  implicit val symbolConfigDecoder: ConfigDecoder[ConfigValue, Symbol] =
    ConfigDecoder.catchNonFatal("Symbol") { value =>
      Symbol(value.atKey(DummyPath).getString(DummyPath))
    }

  implicit val finiteDurationConfigDecoder: ConfigDecoder[ConfigValue, FiniteDuration] =
    ConfigDecoder.catchNonFatal("FiniteDuration") { value =>
      Duration.fromNanos(value.atKey(DummyPath).getDuration(DummyPath, NANOSECONDS))
    }

  implicit val durationConfigDecoder: ConfigDecoder[ConfigValue, Duration] =
    ConfigDecoder.catchNonFatal("Duration") { value =>
      try Duration.fromNanos(value.atKey(DummyPath).getDuration(DummyPath, NANOSECONDS))
      catch { case _: ConfigException.BadValue =>
        Duration(value.atKey(DummyPath).getString(DummyPath))
      }
    }

  implicit val memorySizeConfigDecoder: ConfigDecoder[ConfigValue, ConfigMemorySize] =
    ConfigDecoder.catchNonFatal("ConfigMemorySize") { value =>
      value.atKey(DummyPath).getMemorySize(DummyPath)
    }

  implicit def traversableConfigDecoder[C[_], A](implicit decoder: ConfigDecoder[ConfigValue, A], cbf: CanBuildFrom[Nothing, A, C[A]]): ConfigDecoder[ConfigValue, C[A]] =
    ConfigDecoder.catchNonFatal("Seq") { value =>
      import scala.collection.JavaConverters._

      val list = value.atKey(DummyPath).getList(DummyPath).asScala
      val builder = cbf()
      var idx = 0
      builder.sizeHint(list.size)
      list foreach { entry =>
        builder += decoder.decode(ConfigEntry(idx.toString, SeqElementKeyType, Right(entry)))
          .fold(e => throw new Exception(e.message), identity)
        idx += 1
      }
      builder.result
    }

  implicit def mapValueConfigDecoder[A](implicit decoder: ConfigDecoder[ConfigValue, A]): ConfigDecoder[ConfigValue, Map[String, A]] =
    ConfigDecoder.catchNonFatal("Map") { value =>
      import scala.collection.JavaConverters._

      val relativeConfig = value.atKey(DummyPath).getObject(DummyPath).toConfig
      relativeConfig
        .root
        .entrySet
        .asScala
        .map { entry =>
          val key = entry.getKey
          val quotedKey = ConfigUtil.quoteString(key)
          val value = decoder.decode(ConfigEntry(quotedKey, MapEntryKeyType, Right(relativeConfig.getValue(quotedKey))))
            .fold(e => throw new Exception(e.message), identity)
          key -> value
        }
        .toMap
    }

  implicit def throughStringConfigDecoder[T](implicit dec: ConfigDecoder[String, T]): ConfigDecoder[ConfigValue, T] =
    new ConfigDecoder[ConfigValue, T] {
      def decode[F[_]: Monad, K, S](entry: ConfigEntry[F, K, S, ConfigValue]): F[Either[ConfigError, T]] =
        entry.decodeValue[String].decodeValue[T].value
    }
}