package derevo

trait Part1[T]
trait Part2[T]

object p1 extends Derivation[Part1] {
  def instance[T]: Part1[T] = new Part1[T] {}
}

object p2 extends Derivation[Part2] {
  def instance[T]: Part2[T] = new Part2[T] {}
}

@composite(p1, p2)
object p1AndP2 extends CompositeDerivation

@derive(p1AndP2)
case class HasP1AndP2()

object RefinementTest {
  val part1: Part1[HasP1AndP2] = implicitly
  val part2: Part2[HasP1AndP2] = implicitly
}
