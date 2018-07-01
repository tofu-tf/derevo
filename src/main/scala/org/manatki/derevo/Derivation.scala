package org.manatki.derevo


sealed trait InstanceDef
trait Derivation[TC[_]] extends InstanceDef
