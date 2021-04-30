package derevo.doobie.magnolia

import derevo.Derivation
import magnolia._
import doobie.util._
import java.sql.{PreparedStatement, ResultSet}

object doobieRead extends Derivation[Read] {
  type Typeclass[T] = Read[T]

  def combine[T](ctx: CaseClass[Read, T]): Read[T] = {
    val gets = ctx.parameters.toList.flatMap(_.typeclass.gets)
    val offset = ctx.parameters.scanLeft(0)(_ + _.typeclass.gets.length)

    def unsafeGet(rs: ResultSet, index: Int): T =
      ctx.construct(p => p.typeclass.unsafeGet(rs, index + offset(p.index)))

    new Read(gets, unsafeGet)
  }

  def instance[T]: Read[T] = macro Magnolia.gen[T]
}

object doobieWrite extends Derivation[Write] {
  type Typeclass[A] = Write[A]

  def combine[A](ctx: ReadOnlyCaseClass[Write, A]): Write[A] = {
    val puts         = ctx.parameters.toList.flatMap(_.typeclass.puts)
    val offset       = ctx.parameters.scanLeft(0)(_ + _.typeclass.puts.length)
    def toList(a: A) = ctx.parameters.toList.flatMap(param => param.typeclass.toList(param.dereference(a)))

    def unsafeSet(ps: PreparedStatement, n: Int, a: A) =
      ctx.parameters.foreach(param => param.typeclass.unsafeSet(ps, n + offset(param.index), param.dereference(a)))

    def unsafeUpdate(rs: ResultSet, n: Int, a: A) =
      ctx.parameters.foreach(param => param.typeclass.unsafeUpdate(rs, n + offset(param.index), param.dereference(a)))

    new Write(puts, toList, unsafeSet, unsafeUpdate)
  }

  def instance[T]: Write[T] = macro Magnolia.gen[T]
}