package org.manatki

package object derevo {
  def delegate[TC[_], A](expr: => TC[A]): TC[A] =
    macro DerevoMacro.delegateMacro[TC, A]
}
