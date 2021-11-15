package ciris.hocon

import java.net.{InetAddress, URI, URL}
import java.nio.file.{Path, Paths}

import ciris.{ConfigDecoder, ConfigError}
import ciris.hocon.JavaCompat._
import com.typesafe.config.{ConfigException, ConfigMemorySize}

import scala.concurrent.duration._

trait ConfigValueDecoderInstances
    extends ConfigValueDecoderBaseInstances with ConfigValueDecoderCollectionInstances
    with ConfigValueDecoderJavaInstances

private[hocon] trait ConfigValueDecoderBaseInstances {
  implicit val boolConfigDecoder: ConfigValueDecoder[Boolean]  = nonFatal(_.getBoolean)
  implicit val stringConfigDecoder: ConfigValueDecoder[String] = nonFatal(_.getString)
  implicit val intConfigDecoder: ConfigValueDecoder[Int]       = nonFatal(_.getInt)
  implicit val longConfigDecoder: ConfigValueDecoder[Long]     = nonFatal(_.getLong)
  implicit val floatConfigDecoder: ConfigValueDecoder[Float]   = nonFatal(cfg => cfg.getDouble(_).toFloat)
  implicit val doubleConfigDecoder: ConfigValueDecoder[Double] = nonFatal(_.getDouble)

  implicit val symbolConfigValueDecoder: ConfigValueDecoder[Symbol] =
    nonFatal(cfg => path => Symbol(cfg.getString(path)))

  implicit val finiteDurationConfigValueDecoder: ConfigValueDecoder[FiniteDuration] =
    nonFatal(cfg => path => Duration.fromNanos(cfg.getDuration(path, NANOSECONDS)))

  implicit val durationConfigValueDecoder: ConfigValueDecoder[Duration] =
    nonFatal { cfg => path =>
      try Duration.fromNanos(cfg.getDuration(path, NANOSECONDS))
      catch { case _: ConfigException.BadValue => Duration(cfg.getString(path)) }
    }

  implicit val memorySizeConfigValueDecoder: ConfigValueDecoder[ConfigMemorySize] = nonFatal(
    _.getMemorySize
  )

  implicit def optionConfigDecoder[A, B](implicit
      decoder: ConfigValueDecoder[A]
  ): ConfigValueDecoder[Option[A]] = {
    ConfigDecoder.instance((key, value) => Right(decoder.decode(key, value).toOption))
  }
}

private[hocon] trait ConfigValueDecoderCollectionInstances {
  implicit def seqConfigValueDecoder[C[_], A](implicit
      dec: ConfigValueDecoder[A],
      cbf: FactoryCompat[A, C[A]]
  ): ConfigValueDecoder[C[A]] =
    catchNonFatal { cfg => path =>
      collectErrors(
        cfg
          .getList(path)
          .asScala
          .map(dec.decode(Some(SeqElementKeyType), _))
          .toList
          .reverse
      ).map(cbf.fromSpecific(_))
    }

  implicit def mapConfigValueDecoder[A](implicit
      dec: ConfigValueDecoder[A]
  ): ConfigValueDecoder[Map[String, A]] =
    catchNonFatal { cfg => path =>
      collectErrors(
        cfg
          .getObject(path)
          .entrySet
          .asScala
          .map(entry => dec.decode(Some(MapEntryKeyType), entry.getValue).map(entry.getKey -> _))
          .toList
          .reverse
      ).map(_.toMap)
    }

  def collectErrors[A](inputs: List[Either[ConfigError, A]]): Either[ConfigError, List[A]] = {
    inputs.foldLeft[Either[ConfigError, List[A]]](Right(Nil)) { (acc, param) =>
      (acc, param) match {
        case (Right(list), Right(value)) => Right(value :: list)
        case (Left(errA), Left(errB))    => Left(errA and errB)
        case (err @ Left(_), Right(_))   => err
        case (Right(_), err @ Left(_))   => err.map(_ :: Nil)
      }
    }
  }
}

private[hocon] trait ConfigValueDecoderJavaInstances {
  implicit val inetAddressConfigDecoder: ConfigValueDecoder[InetAddress] =
    nonFatal(cfg => path => InetAddress.getByName(cfg.getString(path)))

  implicit val uriConfigDecoder: ConfigValueDecoder[URI] =
    nonFatal(cfg => path => new URI(cfg.getString(path)))

  implicit val urlConfigDecoder: ConfigValueDecoder[URL] =
    nonFatal(cfg => path => new URL(cfg.getString(path)))

  implicit val pathConfigDecoder: ConfigValueDecoder[Path] =
    nonFatal(cfg => path => Paths.get(cfg.getString(path)))
}
