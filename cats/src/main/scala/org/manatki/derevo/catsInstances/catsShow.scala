package org.manatki.derevo

package catsInstances
import _root_.cats.Show
import org.manatki.derevo.catsInstances.impl.MagnoliaShow

@delegating("org.manatki.derevo.catsInstances.impl.MagnoliaShow.gen")
object catsShow extends Derivation[Show] {
  def instance[T](args: Unit): Show[T] = macro Derevo.delegate[Show, T, Unit]
}
