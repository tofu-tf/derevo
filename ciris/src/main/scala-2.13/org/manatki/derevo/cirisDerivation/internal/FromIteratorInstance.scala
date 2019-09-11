package org.manatki.derevo.cirisDerivation.internal
import scala.collection.Factory

class FromIteratorInstance {
  final implicit def fromIterator[A, C[_]](implicit fact: Factory[A, C[A]]): FromIterator[A, C] = fact.fromSpecific
}
