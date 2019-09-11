package org.manatki.derevo.cirisDerivation.internal

abstract class FromIterator[A, C[_]] {
  def apply(x: Iterator[A]): C[A]
}

object FromIterator extends FromIteratorInstance
