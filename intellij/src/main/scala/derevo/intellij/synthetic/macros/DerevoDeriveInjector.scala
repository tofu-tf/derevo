package derevo.intellij.synthetic.macros

import com.intellij.psi.PsiClass
import org.jetbrains.plugins.scala.lang.psi.api.base.ScAnnotation
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScObject, ScTypeDefinition}
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.typedef.SyntheticMembersInjector

class DerevoDeriveInjector extends SyntheticMembersInjector {
  import DerevoDeriveInjector._

  override def needsCompanionObject(source: ScTypeDefinition): Boolean =
    findMacroAnnotation(source).nonEmpty

  override def injectMembers(source: ScTypeDefinition): Seq[String] = source match {
    case obj: ScObject =>
      obj.fakeCompanionClassOrCompanionClass match {
        case clazz: ScTypeDefinition =>
          findMacroAnnotation(clazz) match {
            case Some((old, annotation)) =>
              annotationParams(annotation).flatMap(injectImplicit(clazz, _, old))
            case _ => Nil
          }
        case _ => Nil
      }
    case _ => Nil
  }

  private def annotationParams(annotation: ScAnnotation): Seq[String] =
    annotation.annotationExpr.getAnnotationParameters.flatMap(_.`type`().toOption).map(_.canonicalText)
}

object DerevoDeriveInjector {
  private[this] val deriveAnnotation = "derevo.derive"
  private[this] val pkg = "_root_.derevo"
  private[this] val mapping = Map[String, String](
    // Cats Core
    s"$pkg.cats.show.type"      -> "implicit val derevoDeriveCatsShow: _root_.cats.Show[%s] = ???",
    s"$pkg.cats.semigroup.type" -> "implicit val derevoDeriveCatsSemigroup: _root_.cats.Semigroup[%s] = ???",
    s"$pkg.cats.monoid.type"    -> "implicit val derevoDeriveCatsMonoid: _root_.cats.Monoid[%s] = ???",
    s"$pkg.cats.eq.type"        -> "implicit val derevoDeriveCatsEq: _root_.cats.Eq[%s] = ???",
    s"$pkg.cats.order.type"     -> "implicit val derevoDeriveCatsOrder: _root_.cats.Order[%s] = ???",

    // Cats Tagless
    s"$pkg.tagless.functor.type"       -> "implicit val derevoDeriveCatsFunctor: _root_.cats.Functor[%s] = ???",
    s"$pkg.tagless.flatMap.type"       -> "implicit val derevoDeriveCatsFlatMap: _root_.cats.FlatMap[%s] = ???",
    s"$pkg.tagless.invariant.type"     -> "implicit val derevoDeriveCatsInvariant: _root_.cats.Invariant[%s] = ???",
    s"$pkg.tagless.contravariant.type" -> "implicit val derevoDeriveCatsContravariant: _root_.cats.Contravariant[%s] = ???",

    s"$pkg.tagless.applyK.type"       -> "implicit val derevoDeriveCatsTaglessApplyK: _root_.cats.tagless.ApplyK[%s] = ???",
    s"$pkg.tagless.functorK.type"     -> "implicit val derevoDeriveCatsTaglessFunctorK: _root_.cats.tagless.FunctorK[%s] = ???",
    s"$pkg.tagless.invariantK.type"   -> "implicit val derevoDeriveCatsTaglessInvariantK: _root_.cats.tagless.InvariantK[%s] = ???",
    s"$pkg.tagless.semigroupalK.type" -> "implicit val derevoDeriveCatsTaglessSemigroupalK: _root_.cats.tagless.SemigroupalK[%s] = ???",

    // Tethys
    s"$pkg.tethys.tethysReader.type" -> "implicit val derevoDeriveTethysReader: _root_.tethys.JsonReader[%s] = ???",
    s"$pkg.tethys.tethysWriter.type" -> "implicit val derevoDeriveTethysWriter: _root_.tethys.JsonWriter[%s] = ???",

    // Circe
    s"$pkg.circe.decoder.type" -> "implicit val derevoDeriveCirceDecoder: _root_.io.circe.Decoder[%s] = ???",
    s"$pkg.circe.encoder.type" -> "implicit val derevoDeriveCirceEncoder: _root_.io.circe.Encoder[%s] = ???",

    // TypedSchema
    s"$pkg.tschema.swagger.type"      -> "implicit val derevoDeriveTypedSchemaSwagger: _root_.ru.tinkoff.tschema.swagger.SwaggerTypeable[%s] = ???",
    s"$pkg.tschema.openapiParam.type" -> "implicit val derevoDeriveTypedSchemaOpenApiParam: _root_.ru.tinkoff.tschema.param.AsOpenApiParam[%s] = ???",
    s"$pkg.tschema.httpParam.type"    -> "implicit val derevoDeriveTypedSchemaHttpParam: _root_.ru.tinkoff.tschema.param.HttpParam[%s] = ???",

    // PureConfig
    s"$pkg.pureconfig.pureconfigReader.type" -> "implicit val derevoDerivePureConfigReader: _root_.pureconfig.ConfigReader[%s] = ???",
    s"$pkg.pureconfig.pureconfigWriter.type" -> "implicit val derevoDerivePureConfigWriter: _root_.pureconfig.ConfigWriter[%s] = ???",

    // Ciris
    s"$pkg.ciris.cirisDecoder.type" -> "implicit val derevoDeriveCirisDecoder: _root_.ciris.hocon.ConfigValueDecoder[%s] = ???",

    // ReactiveMongo
    s"$pkg.reactivemongo.bsonDocumentReader.type" -> "implicit val derevoDeriveReactiveMongoReader: _root_.reactivemongo.bson.BSONDocumentReader[%s] = ???",
    s"$pkg.reactivemongo.bsonDocumentWriter.type" -> "implicit val derevoDeriveReactiveMongoWriter: _root_.reactivemongo.bson.BSONDocumentWriter[%s] = ???",

    // ScalaCheck
    s"$pkg.scalacheck.arbitrary.type"-> "implicit val derevoDeriveScalaCheckArbitrary: _root_.org.scalacheck.Arbitrary[%s] = ???",
  )

  private[this] val oldDeriveAnnotation = "org.manatki.derevo.derive"
  private[this] val oldPkg = "_root_.org.manatki.derevo"
  private[this] val oldMapping = Map[String, String](
    // Cats
    s"$oldPkg.catsInstances.show.type"      -> "implicit val derevoDeriveCatsShow: _root_.cats.Show[%s] = ???",
    s"$oldPkg.catsInstances.semigroup.type" -> "implicit val derevoDeriveCatsSemigroup: _root_.cats.Semigroup[%s] = ???",
    s"$oldPkg.catsInstances.monoid.type"    -> "implicit val derevoDeriveCatsMonoid: _root_.cats.Monoid[%s] = ???",
    s"$oldPkg.catsInstances.eq.type"        -> "implicit val derevoDeriveCatsEq: _root_.cats.Eq[%s] = ???",
    s"$oldPkg.catsInstances.order.type"     -> "implicit val derevoDeriveCatsOrder: _root_.cats.Order[%s] = ???",

    // Cats Tagless
    s"$oldPkg.tagless.functor.type"       -> "implicit val derevoDeriveCatsFunctor: _root_.cats.Functor[%s] = ???",
    s"$oldPkg.tagless.flatMap.type"       -> "implicit val derevoDeriveCatsFlatMap: _root_.cats.FlatMap[%s] = ???",
    s"$oldPkg.tagless.invariant.type"     -> "implicit val derevoDeriveCatsInvariant: _root_.cats.Invariant[%s] = ???",
    s"$oldPkg.tagless.contravariant.type" -> "implicit val derevoDeriveCatsContravariant: _root_.cats.Contravariant[%s] = ???",

    s"$oldPkg.tagless.applyK.type"       -> "implicit val derevoDeriveCatsTaglessApplyK: _root_.cats.tagless.ApplyK[%s] = ???",
    s"$oldPkg.tagless.functorK.type"     -> "implicit val derevoDeriveCatsTaglessFunctorK: _root_.cats.tagless.FunctorK[%s] = ???",
    s"$oldPkg.tagless.invariantK.type"   -> "implicit val derevoDeriveCatsTaglessInvariantK: _root_.cats.tagless.InvariantK[%s] = ???",
    s"$oldPkg.tagless.semigroupalK.type" -> "implicit val derevoDeriveCatsTaglessSemigroupalK: _root_.cats.tagless.SemigroupalK[%s] = ???",

    // Tethys
    s"$oldPkg.tethysInstances.tethysReader.type" -> "implicit val derevoDeriveTethysReader: _root_.tethys.JsonReader[%s] = ???",
    s"$oldPkg.tethysInstances.tethysWriter.type" -> "implicit val derevoDeriveTethysWriter: _root_.tethys.JsonWriter[%s] = ???",

    // Circe
    s"$oldPkg.circeDerivation.decoder.type" -> "implicit val derevoDeriveCirceDecoder: _root_.io.circe.Decoder[%s] = ???",
    s"$oldPkg.circeDerivation.encoder.type" -> "implicit val derevoDeriveCirceEncoder: _root_.io.circe.Encoder[%s] = ???",

    // TypedSchema
    s"$oldPkg.tschemaInstances.swagger.type"      -> "implicit val derevoDeriveTypedSchemaSwagger: _root_.ru.tinkoff.tschema.swagger.SwaggerTypeable[%s] = ???",
    s"$oldPkg.tschemaInstances.openapiParam.type" -> "implicit val derevoDeriveTypedSchemaOpenApiParam: _root_.ru.tinkoff.tschema.param.AsOpenApiParam[%s] = ???",
    s"$oldPkg.tschemaInstances.httpParam.type"    -> "implicit val derevoDeriveTypedSchemaHttpParam: _root_.ru.tinkoff.tschema.param.HttpParam[%s] = ???",

    // PureConfig
    s"$oldPkg.pureconfigDerivation.pureconfigReader.type" -> "implicit val derevoDerivePureConfigReader: _root_.pureconfig.ConfigReader[%s] = ???",
    s"$oldPkg.pureconfigDerivation.pureconfigWriter.type" -> "implicit val derevoDerivePureConfigWriter: _root_.pureconfig.ConfigWriter[%s] = ???",

    // Ciris
    s"$oldPkg.cirisDerivation.cirisDecoder.type" -> "implicit val derevoDeriveCirisDecoder: _root_.ciris.hocon.ConfigValueDecoder[%s] = ???",

    // ReactiveMongo
    s"$oldPkg.reactivemongoDerivation.bsonDocumentReader.type" -> "implicit val derevoDeriveReactiveMongoReader: _root_.reactivemongo.bson.BSONDocumentReader[%s] = ???",
    s"$oldPkg.reactivemongoDerivation.bsonDocumentWriter.type" -> "implicit val derevoDeriveReactiveMongoWriter: _root_.reactivemongo.bson.BSONDocumentWriter[%s] = ???",

    // ScalaCheck
    s"$pkg.scalacheckDerivation.arbitrary.type"-> "implicit val derevoDeriveScalaCheckArbitrary: _root_.org.scalacheck.Arbitrary[%s] = ???",
  )

  def injectImplicit(clazz: PsiClass, param: String, old: Boolean): Option[String] =
    (if (old) oldMapping else mapping).get(param).map(_.format(clazz.getQualifiedName))

  def findMacroAnnotation(source: ScTypeDefinition): Option[(Boolean, ScAnnotation)] =
    source.findAnnotationNoAliases(deriveAnnotation) match {
      case null => source.findAnnotationNoAliases(oldDeriveAnnotation) match {
        case null => None
        case a: ScAnnotation => Some(true -> a)
        case _ => None
      }
      case a: ScAnnotation => Some(false -> a)
      case _ => None
    }
}
