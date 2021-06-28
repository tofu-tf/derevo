package ciris.hocon

import scala.collection.Factory
import scala.collection.convert.AsScalaExtensions

object JavaCompat extends AsScalaExtensions

trait FactoryCompat[-A, +C] {
  def fromSpecific(it: IterableOnce[A]): C
}

object FactoryCompat {
  implicit def fromFactory[A, C](implicit factory: Factory[A, C]): FactoryCompat[A, C] = new FactoryCompat[A, C] {
    override def fromSpecific(it: IterableOnce[A]): C = factory.fromSpecific(it)
  }
}
