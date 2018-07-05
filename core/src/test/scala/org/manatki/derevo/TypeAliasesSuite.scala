package org.manatki.derevo
import org.manatki.derevo.TypeAliasesSuite.{string1, string2}

object TypeAliasesSuite {
  class MultiParamClass[A, B]

  type StringClass1[A] = MultiParamClass[A, String]
  type StringClass2[A] = MultiParamClass[String, A]

  object string1 extends Derivation[StringClass1] {
    def instance[A]: StringClass1[A] = new MultiParamClass
  }

  object string2 extends Derivation[StringClass2] {
    def instance[A]: StringClass2[A] = new MultiParamClass
  }
}

@derive(string1, string2)
final case class StringAliases()

