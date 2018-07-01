package org.manatki.derevo

package cats
import _root_.cats.Show
import org.manatki.derevo.cats.impl.MagnoliaShow

object show extends Derivation[Show] {
  def apply[T](): Show[T] = delegate(MagnoliaShow.gen[T])
}
