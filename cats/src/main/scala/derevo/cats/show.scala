package derevo
package cats

import _root_.cats.Show
import magnolia.{CaseClass, Magnolia, SealedTrait}

/** Generates [[Show]] instance for annotated type with field names
  *
  * @example
  * {{{
  *   import derevo.derive
  *   import cats.Show
  *   import derevo.cats.show
  *
  *   @derive(show)
  *   case class Foo(bar: Int, baz: String)
  *
  *   Show[Foo].show(Foo(3, "abc"))
  *   > Foo{bar=3,baz=abc}
  * }}}
  */
object show extends ShowConfigured {

  override def fieldShow(fieldName: String, shownFieldValue: String): String = s"$fieldName=$shownFieldValue"

  override def resultCombine(typeName: String, shownFieldValues: Seq[String]): String =
    shownFieldValues.mkString(s"$typeName{", ",", "}")

}

/** Generates [[Show]] instance for annotated type with newline delimited fields
  *
  * Suitable for case classes with multiple fields for comfortable reading.
  *
  * @example
  * {{{
  *   import derevo.derive
  *   import cats.Show
  *   import derevo.cats.showNewline
  *
  *   @derive(showNewline)
  *   case class Foo(bar: Int, baz: String)
  *
  *   Show[Foo].show(Foo(3, "abc"))
  *   > Foo{
  *       bar = 3,
  *       baz = abc
  *     }
  * }}}
  */
object showNewline extends ShowConfigured {

  override def fieldShow(fieldName: String, shownFieldValue: String): String = s"  $fieldName = $shownFieldValue"

  override def resultCombine(typeName: String, shownFieldValues: Seq[String]): String =
    shownFieldValues.mkString(s"$typeName{\n", ",\n", "\n}")
}

trait ShowConfigured extends Derivation[Show] {
  def fieldShow(fieldName: String, shownFieldValue: String): String

  def resultCombine(typeName: String, shownFieldValues: Seq[String]): String

  type Typeclass[T] = Show[T]

  def combine[T](ctx: CaseClass[Show, T]): Show[T] =
    (value: T) =>
      resultCombine(
        ctx.typeName.short,
        ctx.parameters.map { p =>
          fieldShow(p.label, s"${p.typeclass.show(p.dereference(value))}")
        }
      )

  def dispatch[T](ctx: SealedTrait[Show, T]): Show[T] =
    (value: T) =>
      ctx.dispatch(value) { sub =>
        sub.typeclass.show(sub.cast(value))
      }

  def instance[T]: Show[T] = macro Magnolia.gen[T]
}
