package ciris.hocon

import ciris.api.Monad
import ciris.{ConfigDecoder, ConfigEntry, ConfigError}
import com.typesafe.config.{ConfigException, ConfigMemorySize, ConfigValue}

import scala.collection.generic.CanBuildFrom
import scala.collection.JavaConverters._
import scala.concurrent.duration._

abstract class ConfigValueDecoder[A] extends ConfigDecoder[ConfigValue, A]

object ConfigValueDecoder extends ConfigValueDecoderInstances

trait ConfigValueDecoderInstances
    extends ConfigValueDecoderBaseInstances with ConfigValueDecoderCollectionInstances
    with ConfigValueDecoderLowPriorityInstances

private[hocon] trait ConfigValueDecoderBaseInstances {
  implicit val stringConfigDecoder: ConfigValueDecoder[String] = catchNonFatal(_.getString)

  implicit val symbolConfigValueDecoder: ConfigValueDecoder[Symbol] =
    catchNonFatal(cfg => path => Symbol(cfg.getString(path)))

  implicit val finiteDurationConfigValueDecoder: ConfigValueDecoder[FiniteDuration] =
    catchNonFatal(cfg => path => Duration.fromNanos(cfg.getDuration(path, NANOSECONDS)))

  implicit val durationConfigValueDecoder: ConfigValueDecoder[Duration] =
    catchNonFatal { cfg => path =>
      try Duration.fromNanos(cfg.getDuration(path, NANOSECONDS))
      catch { case _: ConfigException.BadValue => Duration(cfg.getString(path)) }
    }

  implicit val memorySizeConfigValueDecoder: ConfigValueDecoder[ConfigMemorySize] = catchNonFatal(_.getMemorySize)
}

private[hocon] trait ConfigValueDecoderCollectionInstances {
  implicit def seqConfigValueDecoder[C[_], A](
      implicit
      dec: ConfigValueDecoder[A],
      cbf: CanBuildFrom[Nothing, A, C[A]]
  ): ConfigValueDecoder[C[A]] =
    catchNonFatal { cfg => path =>
      val list    = cfg.getList(path)
      val builder = cbf()
      var idx     = 0

      builder.sizeHint(list.size)
      list.asScala.foreach { value =>
        builder += ConfigEntry(idx, SeqElementKeyType, Right(value)).decodeValue[A].orThrow()
        idx = 1
      }
      builder.result
    }

  implicit def mapConfigValueDecoder[A](
      implicit dec: ConfigValueDecoder[A]
  ): ConfigValueDecoder[Map[String, A]] =
    catchNonFatal { cfg => path =>
      val entries = cfg.getObject(path).entrySet
      val builder = Map.newBuilder[String, A]

      builder.sizeHint(entries.size)
      entries.asScala.foreach { entry =>
        val key   = entry.getKey
        val value = ConfigEntry(key, MapEntryKeyType, Right(entry.getValue)).decodeValue[A].orThrow()

        builder += ((key, value))
      }
      builder.result
    }
}

private[hocon] trait ConfigValueDecoderLowPriorityInstances { self: ConfigValueDecoderBaseInstances =>
  implicit def throughStringConfigValueDecoder[T](implicit dec: ConfigDecoder[String, T]): ConfigValueDecoder[T] =
    new ConfigValueDecoder[T] {
      def decode[F[_]: Monad, K, S](entry: ConfigEntry[F, K, S, ConfigValue]): F[Either[ConfigError, T]] =
        entry.decodeValue[String].decodeValue[T].value
    }
}
