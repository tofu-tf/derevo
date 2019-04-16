package org.manatki.derevo

import _root_.ciris.ConfigDecoder
import com.typesafe.config.ConfigValue

package object cirisDerivation {
  type ConfigValueDecoder[T] = ConfigDecoder[ConfigValue, T]
}