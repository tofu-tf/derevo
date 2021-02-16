package derevo

/** Utilitary holder for the newtype instance derivation */
class NewTypeRepr[TC[_], R](private val repr: TC[R]) extends AnyVal {
  def instance[A]: TC[A] = repr.asInstanceOf[TC[A]]
}

trait NewTypeDerivation[TC[_]] {
  final def newtype[R](implicit repr: TC[R]): NewTypeRepr[TC, R] = new NewTypeRepr[TC, R](repr)
}
