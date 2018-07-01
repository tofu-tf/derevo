package org.manatki.derevo


sealed trait InstanceDef
class Derivation[TC[_]] extends InstanceDef
