package org.manatki.derevo.tagless

import cats.{Contravariant, FlatMap, Functor, Invariant, Semigroupal}
import cats.tagless.{ApplyK, FunctorK, InvariantK, SemigroupalK}
import org.manatki.derevo.{Derevo, DerivationK1, DerivationK2, delegating}

@delegating("cats.tagless.Derive.functorK")
object functorK extends DerivationK2[FunctorK] {
  def instance[T[_[_]]]: FunctorK[T] = macro Derevo.delegateK2[FunctorK, T]
}

@delegating("cats.tagless.Derive.invariantK")
object invariantK extends DerivationK2[InvariantK] {
  def instance[T[_[_]]]: InvariantK[T] = macro Derevo.delegateK2[InvariantK, T]
}

@delegating("cats.tagless.Derive.semigroupalK")
object semigroupalK extends DerivationK2[SemigroupalK] {
  def instance[T[_[_]]]: SemigroupalK[T] = macro Derevo.delegateK2[SemigroupalK, T]
}

@delegating("cats.tagless.Derive.applyK")
object applyK extends DerivationK2[ApplyK] {
  def instance[T[_[_]]]: ApplyK[T] = macro Derevo.delegateK2[ApplyK, T]
}

@delegating("cats.tagless.Derive.functor")
object functor extends DerivationK1[Functor] {
  def instance[T[_]]: Functor[T] = macro Derevo.delegateK1[Functor, T]
}

@delegating("cats.tagless.Derive.invariant")
object invariant extends DerivationK1[Invariant] {
  def instance[T[_]]: Invariant[T] = macro Derevo.delegateK1[Invariant, T]
}

@delegating("cats.tagless.Derive.contravariant")
object contravariant extends DerivationK1[Contravariant] {
  def instance[T[_]]: Contravariant[T] = macro Derevo.delegateK1[Contravariant, T]
}

@delegating("cats.tagless.Derive.flatMap")
object flatMap extends DerivationK1[FlatMap] {
  def instance[T[_]]: FlatMap[T] = macro Derevo.delegateK1[FlatMap, T]
}
