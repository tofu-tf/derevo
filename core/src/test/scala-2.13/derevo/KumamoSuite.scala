package derevo

class Kumamo[U[f[_]]]

object Kumamo extends DerivationHK[Kumamo] {
  def instance[U[f[_]]]: Kumamo[U] = new Kumamo
}

class Anksu[G[f[_, _], _, _]]

object Anksu extends DerivationBiTr[Anksu] {
  def instance[U[f[_, _], _, _]]: Anksu[U] = new Anksu
}

@derive(Kumamo)
class Xormeg[A, B[_], C[-_, +_[_]], F[_]]

@derive(Anksu)
class Jusprako[X, Y[-_ <: X] >: X, +F[_, _], -A, +B]
