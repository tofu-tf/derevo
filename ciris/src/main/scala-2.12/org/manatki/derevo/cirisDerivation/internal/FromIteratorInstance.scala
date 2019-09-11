package org.manatki.derevo.cirisDerivation.internal
import scala.collection.generic.CanBuild

class FromIteratorInstance {
  final implicit def fromIterator[A, C[_]](implicit fact: CanBuild[A, C[A]]): FromIterator[A, C] = _.to[C]
}
