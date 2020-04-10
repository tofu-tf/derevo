package derevo.tethys

import java.util.regex.Pattern

import derevo.Derivation
import magnolia.{CaseClass, Magnolia, SealedTrait, Subtype}
import tethys.commons.Token
import tethys.readers.tokens.QueueIterator.WrongTokenError
import tethys.readers.tokens.{BaseTokenIterator, TokenIterator}
import tethys.readers.{FieldName, ReaderError}
import tethys.writers.tokens.TokenWriter
import tethys.{JsonReader, JsonWriter}

import scala.collection.mutable
import scala.language.experimental.macros

case class CodecConfig(
    discriminator: Option[String] = None,
    fieldRename: String => String = identity,
    constrRename: String => String = identity
) {
  def withDiscriminator(name: String) = copy(discriminator = Some(name))
  def snakecase = copy(
    fieldRename = CodecConfig.lowerSnakecase,
    constrRename = CodecConfig.lowerSnakecase
  )
}

object CodecConfig {
  val default: CodecConfig = CodecConfig()

  // Names transformations copied from tethys, originally from enumeratum
  private val capitalized: Pattern = Pattern.compile("([A-Z]+)([A-Z][a-z])")
  private val regexp2: Pattern     = Pattern.compile("([a-z\\d])([A-Z])")
  private val replacement: String  = "$1_$2"

  private def splitName(name: String): List[String] = {
    val first = capitalized.matcher(name).replaceAll(replacement)
    regexp2.matcher(first).replaceAll(replacement).split("_").toList
  }

  val snakecase: String => String = splitName(_).mkString("_")
  val kebabcase: String => String = splitName(_).mkString("-")

  val lowerSnakecase: String => String = snakecase andThen (_.toLowerCase)
  val upperSnakecase: String => String = snakecase andThen (_.toUpperCase)
}

object jsonWriter extends Derivation[JsonWriter] {
  type Typeclass[A] = JsonWriter[A]

  abstract class RecordJsonWriter[A] extends JsonWriter[A] {
    def write(value: A, tokenWriter: TokenWriter): Unit = writeRecord(value, tokenWriter, None)

    def writeRecord(value: A, tokenWriter: TokenWriter, tpe: Option[String]): Unit
  }

  def combine[A](
      caseclass: CaseClass[JsonWriter, A]
  )(implicit conf: CodecConfig = CodecConfig.default): RecordJsonWriter[A] =
    (value, writer, tpeOpt) => {
      writer.writeObjectStart()

      for (tpe <- tpeOpt; field <- conf.discriminator) {
        writer.writeFieldName(field)
        writer.writeString(tpe)
      }

      for (param <- caseclass.parameters)
        param.typeclass.write(conf.fieldRename(param.label), param.dereference(value), writer)

      writer.writeObjectEnd()
    }

  def dispatch[A](
      sealedTrait: SealedTrait[JsonWriter, A]
  )(implicit conf: CodecConfig = CodecConfig.default): JsonWriter[A] =
    (value, writer) => {
      sealedTrait.dispatch(value) { sub =>
        val label = conf.constrRename(sub.typeName.short)
        sub.typeclass match {
          case derived: RecordJsonWriter[sub.SType] if conf.discriminator.nonEmpty =>
            derived.writeRecord(sub.cast(value), writer, Some(label))
          case _ =>
            writer.writeObjectStart()
            sub.typeclass.write(label, sub.cast(value), writer)
            writer.writeObjectEnd()
        }
      }
    }

  def instance[A]: JsonWriter[A] = macro Magnolia.gen[A]
}

object jsonReader extends Derivation[JsonReader] {
  type Typeclass[A] = JsonReader[A]

  abstract class RecordJsonReader[A] extends JsonReader[A] {

    def read(it: TokenIterator)(implicit fieldName: FieldName): A = readRecord(it, false)

    def readRecord(it: TokenIterator, inside: Boolean)(implicit fieldName: FieldName): A
  }

  private def illegal = throw new WrongTokenError("")

  private val constNull = new BaseTokenIterator {

    def currentToken(): Token = Token.NullValueToken

    def nextToken(): Token = Token.NullValueToken

    def fieldName(): String = illegal
    def string(): String    = illegal
    def number(): Number    = illegal
    def short(): Short      = illegal
    def int(): Int          = illegal
    def long(): Long        = illegal
    def float(): Float      = illegal
    def double(): Double    = illegal
    def boolean(): Boolean  = illegal
  }

  private case class PrependStartObj(inner: TokenIterator) extends BaseTokenIterator {
    var first: Boolean = true

    def currentToken(): Token = if (first) Token.ObjectStartToken else inner.currentToken()

    def nextToken(): Token = if (first) {
      first = false
      inner.currentToken()
    } else inner.nextToken()

    def fieldName(): String = if (!first) inner.fieldName() else illegal
    def string(): String    = if (!first) inner.string() else illegal
    def number(): Number    = if (!first) inner.number() else illegal
    def short(): Short      = if (!first) inner.short() else illegal
    def int(): Int          = if (!first) inner.int() else illegal
    def long(): Long        = if (!first) inner.long() else illegal
    def float(): Float      = if (!first) inner.float() else illegal
    def double(): Double    = if (!first) inner.double() else illegal
    def boolean(): Boolean  = if (!first) inner.boolean() else illegal
  }

  private def fail(exp: String, it: TokenIterator)(implicit fn: FieldName) =
    ReaderError.wrongJson(s"Expected $exp but found: ${it.currentToken()}")

  def combine[A](
      caseclass: CaseClass[JsonReader, A]
  )(implicit conf: CodecConfig = CodecConfig.default): RecordJsonReader[A] =
    new RecordJsonReader[A] {
      val paramMap = caseclass.parameters.iterator.map(p => conf.fieldRename(p.label) -> p).toMap

      def readRecord(it: TokenIterator, inside: Boolean)(implicit fieldName: FieldName): A = {
        if (it.currentToken().isObjectStart) it.next()
        else if (!inside) fail("object start", it)

        val elems = mutable.Map[String, Any]()

        def go(): Unit = {
          if (it.currentToken().isFieldName) {
            val name = it.fieldName()
            it.next()
            paramMap.get(name) match {
              case None => it.skipExpression()
              case Some(param) =>
                val res = param.typeclass.read(it)(FieldName(name))
                elems.put(name, res)
            }
            go()
          } else if (it.currentToken().isObjectEnd) it.next()
          else fail("end of object or field name", it)
        }

        go()

        for ((name, p) <- paramMap -- elems.keySet)
          elems.put(name, p.typeclass.read(constNull)(FieldName(name)))

        caseclass.construct(p => elems(conf.fieldRename(p.label)))
      }
    }

  def dispatch[A](
      sealedTrait: SealedTrait[JsonReader, A]
  )(implicit conf: CodecConfig = CodecConfig.default): JsonReader[A] =
    new JsonReader[A] {
      val subMap = sealedTrait.subtypes.iterator.map(s => conf.constrRename(s.typeName.short) -> s).toMap

      def subByName(name: String)(implicit fieldName: FieldName) = subMap.get(name) match {
        case Some(s) => s
        case None =>
          ReaderError.wrongJson(
            subMap.keysIterator.mkString("discriminator should be on of :'", "', '", s"', got $name")
          )
      }

      def read(it: TokenIterator)(implicit fieldName: FieldName): A = {
        if (!it.currentToken().isObjectStart) fail("object start", it)
        if (!it.nextToken().isFieldName) fail("field name", it)
        conf.discriminator match {
          case Some(dis) =>
            if (it.fieldName() != dis)
              ReaderError.wrongJson(s"first field in structure with discriminator should be $dis")
            it.next()
            if (!it.currentToken().isStringValue) fail("string", it)
            val sub = subByName(it.string())
            it.next()
            sub.typeclass.read(PrependStartObj(it))

          case None =>
            val sub = subByName(it.fieldName())
            it.next()
            val res = sub.typeclass.read(it)
            if (!it.currentToken().isObjectEnd) fail("end of object", it)
            it.next()
            res
        }
      }
    }

  def instance[A]: JsonReader[A] = macro Magnolia.gen[A]
}
