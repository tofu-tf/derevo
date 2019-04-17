package org.manatki.derevo.tagless

import cats.arrow.FunctionK

// credit to Alex Konovalov for this trick: undefined type members
// with unstable prefixes, to encode universal quantification
final class MkFunctionK[F[_]](val dummy: Boolean = true) extends AnyVal {
  type T

  def apply[G[_]](f: F[T] => G[T]): FunctionK[F, G] = new FunctionK[F, G] {
    def apply[A](fa: F[A]): G[A] = f(fa.asInstanceOf[F[T]]).asInstanceOf[G[A]]
  }
}

object functionK {
  def apply[F[_]]: MkFunctionK[F] = new MkFunctionK
}
