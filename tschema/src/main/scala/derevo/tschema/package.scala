package derevo
import ru.tinkoff.tschema.param.HttpParam
import ru.tinkoff.tschema.swagger.{AsOpenApiParam, SwaggerTypeable}

package object tschema {
  @deprecated("use @derive(tschema.swagger.Swagger)", since = "0.11.1")
  @delegating("ru.tinkoff.tschema.swagger.MagnoliaSwagger.derive")
  object swagger extends Derivation[SwaggerTypeable] {
    def instance[T]: SwaggerTypeable[T] = macro Derevo.delegate[SwaggerTypeable, T]
  }

  @deprecated("use @derive(tschema.swagger.AsOpenApiParam)", since = "0.11.1")
  @delegating("ru.tinkoff.tschema.swagger.AsOpenApiParam.generate")
  object openapiParam extends Derivation[AsOpenApiParam]{
    def instance[T]: AsOpenApiParam[T] = macro Derevo.delegate[AsOpenApiParam, T]
  }

  @deprecated("use @derive(tschema.param.HttpParam)", since = "0.11.1")
  @delegating("ru.tinkoff.tschema.param.HttpParam.generate")
  object httpParam extends Derivation[HttpParam]{
    def instance[T]: HttpParam[T] = macro Derevo.delegate[HttpParam, T]
  }
}
