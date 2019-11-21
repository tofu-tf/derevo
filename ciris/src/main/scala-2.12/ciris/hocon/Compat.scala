package ciris.hocon

import scala.collection.convert.DecorateAsScala
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

object JavaCompat extends DecorateAsScala

trait FactoryCompat[-A, +C] {
  def fromSpecific(it: Iterable[A]): C
}

object FactoryCompat {
  implicit def fromCanBuildFrom[A, C](implicit cbf: CanBuildFrom[Nothing, A, C]): FactoryCompat[A, C] =
    new FactoryCompat[A, C] {
      override def fromSpecific(it: Iterable[A]): C =
        (cbf.apply() ++= it).result()
    }
}
