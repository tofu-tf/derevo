package org.manatki.derevo
package tschemaInstances
import ru.tinkoff.tschema.swagger.SwaggerTypeable

@delegating("ru.tinkoff.tschema.swagger.MagnoliaSwagger.derive")
object swagger extends Derivation[SwaggerTypeable] {
  def instance[T]: SwaggerTypeable[T] = macro Derevo.delegate[SwaggerTypeable, T]
}
