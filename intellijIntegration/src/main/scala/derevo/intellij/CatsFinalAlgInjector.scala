package derevo.intellij

import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScObject, ScTypeDefinition}
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.typedef.SyntheticMembersInjector

class CatsFinalAlgInjector extends SyntheticMembersInjector {
  import CatsFinalAlgInjector._

  override def injectMembers(source: ScTypeDefinition): Seq[String] = source match {
    case obj: ScObject =>
      obj.fakeCompanionClassOrCompanionClass match {
        case clazz: ScTypeDefinition if isTypeclassWithAnnotation(clazz) =>
          val cname = clazz.name
          val tpe = clazz.typeParameters.head
          val tname = tpe.name
          val ttext = ScalaPsiUtil.typeParamString(tpe)

          Seq(s"@scala.inline def apply[$ttext](implicit instance: $cname[$tname]): $cname[$tname] = instance")
        case _ => Nil
      }
    case _ => Nil
  }

  override def needsCompanionObject(source: ScTypeDefinition): Boolean = isTypeclassWithAnnotation(source)
}

object CatsFinalAlgInjector {
  private[this] val finalAlgAnnotation = "cats.tagless.finalAlg"

  def isTypeclassWithAnnotation(source: ScTypeDefinition): Boolean =
    source.findAnnotationNoAliases(finalAlgAnnotation) != null && source.typeParameters.length == 1
}
