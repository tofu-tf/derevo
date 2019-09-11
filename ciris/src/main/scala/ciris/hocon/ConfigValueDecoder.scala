package ciris.hocon

import ciris.api.Monad
import ciris.{ConfigDecoder, ConfigEntry, ConfigError}
import com.typesafe.config.{ConfigException, ConfigMemorySize, ConfigValue}
import org.manatki.derevo.cirisDerivation.internal.FromIterator

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
      fromIter: FromIterator[A, C]
  ): ConfigValueDecoder[C[A]] =
    catchNonFatal { cfg => path =>
      fromIter(cfg.getList(path).iterator().asScala.zipWithIndex.map {
        case (value, idx) => ConfigEntry(idx, SeqElementKeyType, Right(value)).decodeValue[A].orThrow()
      })
    }

  implicit def mapConfigValueDecoder[A](
      implicit dec: ConfigValueDecoder[A]
  ): ConfigValueDecoder[Map[String, A]] =
    catchNonFatal { cfg => path =>
      cfg
        .getObject(path)
        .entrySet()
        .iterator
        .asScala
        .map { entry =>
          val key = entry.getKey
          key -> ConfigEntry(key, MapEntryKeyType, Right(entry.getValue)).decodeValue[A].orThrow()
        }
        .toMap
    }
}

private[hocon] trait ConfigValueDecoderLowPriorityInstances { self: ConfigValueDecoderBaseInstances =>
  implicit def throughStringConfigValueDecoder[T](implicit dec: ConfigDecoder[String, T]): ConfigValueDecoder[T] =
    new ConfigValueDecoder[T] {
      def decode[F[_]: Monad, K, S](entry: ConfigEntry[F, K, S, ConfigValue]): F[Either[ConfigError, T]] =
        entry.decodeValue[String].decodeValue[T].value
    }
}
