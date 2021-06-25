package derevo

import scala.language.higherKinds
import scala.reflect.macros.blackbox
import Derevo._

class Derevo(val c: blackbox.Context) {
  import c.universe._

  type Newtype      = NewtypeP[Tree]
  type NameAndTypes = NameAndTypesP[c.Type]

  val DelegatingSymbol      = typeOf[delegating].typeSymbol
  val PhantomSymbol         = typeOf[phantom].typeSymbol
  val PassTypeArgsSymbol    = typeOf[PassTypeArgs].typeSymbol
  val KeepRefinementsSymbol = typeOf[KeepRefinements].typeSymbol

  val instanceDefs         = Vector(
  )
  val IsSpecificDerivation = isInstanceDef[SpecificDerivation[Any, Any, Any]]()
  val IsDerivation         = isInstanceDef[Derivation[Any]]()

  val HKDerivation = new DerivationList(
    isInstanceDef[DerivationKN1[Any]](1),
    isInstanceDef[DerivationKN2[Any]](2),
    isInstanceDef[DerivationKN3[Any]](1),
    isInstanceDef[DerivationKN4[Any]](2),
    isInstanceDef[DerivationKN5[Any]](3),
    isInstanceDef[DerivationKN11[Any]](1),
    isInstanceDef[DerivationKN17[Any]](3)
  )

  val ParamRequire = isInstanceDef[ParamRequire[Any]](0)
  val IIH          = weakTypeOf[InjectInstancesHere].typeSymbol

  val EstaticoFQN     = "io.estatico.newtype.macros.newtype"
  val SupertaggedFQN  = "supertagged.TaggedType"
  val Supertagged0FQN = "supertagged.TaggedType0"

  def delegate[TC[_], I]: c.Expr[TC[I]] =
    c.Expr(delegation(c.prefix.tree, None))

  def delegateK1[TC[_[_]], I[_]]: c.Expr[TC[I]] =
    c.Expr(delegation(c.prefix.tree, None))

  def delegateK2[TC[_[_[_]]], I[_[_]]]: c.Expr[TC[I]] =
    c.Expr(delegation(c.prefix.tree, None))

  def delegateParam[TC[_], I, Arg](arg: c.Expr[Arg]): c.Expr[TC[I]] =
    c.Expr(delegation(c.prefix.tree, Some((method, args) => q"$method($arg, ..$args)")))

  def delegateParams2[TC[_], I, Arg1, Arg2](arg1: c.Expr[Arg1], arg2: c.Expr[Arg2]): c.Expr[TC[I]] =
    c.Expr(delegation(c.prefix.tree, Some((method, args) => q"$method($arg1, $arg2, ..$args)")))

  def delegateParams3[TC[_], I, Arg1, Arg2, Arg3](
      arg1: c.Expr[Arg1],
      arg2: c.Expr[Arg2],
      arg3: c.Expr[Arg3]
  ): c.Expr[TC[I]] =
    c.Expr(delegation(c.prefix.tree, Some((method, args) => q"$method($arg1, $arg2, $arg3, ..$args)")))

  private def unpackArgs(args: Tree): Seq[Tree] =
    args match {
      case q"(..$params)" => params
      case _              => abort("argument of delegateParams must be tuple")
    }

  def delegateParams[TC[_], I, Args](args: c.Expr[Args]): c.Expr[TC[I]] =
    c.Expr(delegation(c.prefix.tree, Some((method, rest) => q"$method(..${unpackArgs(args.tree) ++ rest})")))

  private def delegation(tree: Tree, maybeCall: Option[(Tree, List[Tree]) => Tree]): Tree = {
    val annots            = tree.tpe.termSymbol.annotations
    val (delegatee, args) = annots
      .map(_.tree)
      .collectFirst {
        case q"new $cls(${to: Tree}, ..$rest)" if cls.symbol == DelegatingSymbol =>
          c.eval(c.Expr[String](to)) -> rest
      }
      .getOrElse(abort(s"could not find @delegating annotation at $tree"))

    val method = delegatee.split("\\.").map(TermName(_)).foldLeft[Tree](q"_root_")((a, b) => q"$a.$b")

    def default = args match {
      case Nil => method
      case _   => q"$method(..$args)"
    }
    maybeCall.fold(default)(call => call(method, args))
  }

  def isConstructionOf(name: String)(t: Tree): Boolean = t match {
    case q"new $cls(...$_)" =>
      val tts = c.typecheck(t, silent = true).symbol
      tts.isMethod && {
        val o = tts.asMethod.owner
        o.isClass && o.asClass.fullName == name
      }
    case _                  => false
  }

  def deriveMacro(annottees: Tree*): Tree = {
    annottees match {
      case Seq(obj: ModuleDef) =>
        obj match {
          case q"$mods object $companion extends {..$earlyDefs} with ..$parents{$self => ..$defs}" =>
            q"""
              $mods object $companion extends {..$earlyDefs} with ..$parents{$self =>
                ..${injectInstances(defs, instances(obj))}
             }"""
        }

      case Seq(cls: ClassDef) =>
        q"""
           $cls
           object ${cls.name.toTermName} {
               ..${instances(cls)}
           }
         """

      case Seq(
            cls: ClassDef,
            q"$mods object $companion extends {..$earlyDefs} with ..$parents{$self => ..$defs}"
          ) =>
        q"""
           $cls
           $mods object $companion extends {..$earlyDefs} with ..$parents{$self =>
             ..${injectInstances(defs, instances(cls))}
           }
         """
    }
  }

  private def injectInstances(defs: Seq[Tree], instances: List[Tree]): Seq[Tree] = {
    val (pre, post) = defs.span {
      case tree @ q"$call()" =>
        call match {
          case q"insertInstancesHere"    => false
          case q"$_.insertInstancesHere" => false
          case _                         => true
        }
      case _                 => true
    }

    pre ++ instances ++ post.drop(1)
  }

  object ClassOf {
    def unapply(t: Tree): Option[String] = {
      val tc = c.typecheck(t, mode = c.TYPEmode, silent = true)
      Option(tc.symbol).map(_.fullName)
    }

  }

  private def instances(cls: ImplDef): List[Tree] = {
    val newType = cls match {
      case c: ClassDef if c.mods.annotations.exists(isConstructionOf(EstaticoFQN)) =>
        c.impl.body.collectFirst {
          case q"$mods val $n :$t" if mods.hasFlag(Flag.CASEACCESSOR) => NewtypeCls(t)
        }
      case m: ModuleDef                                                            =>
        m.impl.parents.collectFirst { case tq"${ClassOf(SupertaggedFQN | Supertagged0FQN)}[$t]" =>
          NewtypeMod(t, tq"${m.name}.Type")
        }
      case _                                                                       => None
    }
    c.prefix.tree match {
      case q"new derive(..${instances})" =>
        instances.map(buildInstance(_, cls, newType))
    }
  }

  private def buildInstance(tree: Tree, impl: ImplDef, newType: Option[Newtype]): Tree = {
    val typRef = impl match {
      case cls: ClassDef  => tq"${impl.name.toTypeName}"
      case obj: ModuleDef =>
        newType match {
          case Some(NewtypeMod(_, res)) => res
          case _                        => tq"${obj.name}.type"
        }
    }

    val (mode, call) = tree match {
      case q"$obj.$method(..$args)" => (nameAndTypes(obj), tree)

      case q"$obj(..$args)" => (nameAndTypes(obj), tree)

      case q"$obj" =>
        val call = newType.fold(q"$obj.instance")(t => q"$obj.newtype[${t.underlying}].instance")
        (nameAndTypes(obj), call)
    }

    val tn         = TermName(mode.name)
    val allTparams = impl match {
      case cls: ClassDef  => cls.tparams
      case obj: ModuleDef => Nil
    }

    val tparams = allTparams.dropRight(mode.drop)
    val pparams = allTparams.takeRight(mode.drop)

    val tps       = tparams.map(_.name)
    def appTyp    = tq"$typRef[..$tps]"
    def allTnames = allTparams.map(_.name)
    def lamTyp    = tq"({ type Lam[..$pparams] = $typRef[..$allTnames] })#Lam"
    val outTyp    = if (pparams.isEmpty) appTyp else lamTyp

    val resT = mkAppliedType(mode.to, outTyp)

    val callWithT = if (mode.passArgs) q"$call[$outTyp]" else call

    def fixFirstTypeParam = {
      val nothingT = c.typeOf[Nothing]

      c.typecheck(call, silent = true) match {
        case q"$method[$nothing, ..$remainingTpes](..$args)" if nothing.tpe == nothingT =>
          q"$method[$outTyp, ..$remainingTpes](..$args)"
        case q"$method[$nothing, ..$remainingTpes]" if nothing.tpe == nothingT          =>
          q"$method[$outTyp, ..$remainingTpes]"
        case _                                                                          => tree
      }
    }

    if (allTparams.isEmpty || allTparams.length <= mode.drop) {
      if (mode.keepRefinements) {
        q"""
        @java.lang.SuppressWarnings(scala.Array("org.wartremover.warts.All", "scalafix:All", "all"))
        implicit val $tn = $fixFirstTypeParam
        """
      } else {
        val resTc = if (newType.isDefined) mode.newtype else mode.to
        val resT  = mkAppliedType(resTc, tq"$typRef")

        q"""
        @java.lang.SuppressWarnings(scala.Array("org.wartremover.warts.All", "scalafix:All", "all"))
        implicit val $tn: $resT = $callWithT
        """
      }
    } else {

      val implicits =
        if (mode.cascade)
          tparams.flatMap { tparam =>
            val phantom = tparam.mods.annotations.exists { t => c.typecheck(t).tpe.typeSymbol == PhantomSymbol }
            if (phantom) None
            else {
              val name = TermName(c.freshName("ev"))
              val typ  = tparam.name
              val reqT = mkAppliedType(mode.from, tq"$typ")
              Some(q"val $name: $reqT")
            }
          }
        else Nil

      if (mode.keepRefinements) {
        q"""
        @java.lang.SuppressWarnings(scala.Array("org.wartremover.warts.All", "scalafix:All", "all"))
        implicit def $tn[..$tparams](implicit ..$implicits) = $fixFirstTypeParam
        """
      } else {
        q"""
        @java.lang.SuppressWarnings(scala.Array("org.wartremover.warts.All", "scalafix:All", "all"))
        implicit def $tn[..$tparams](implicit ..$implicits): $resT = $callWithT
        """
      }
    }
  }

  private def mkAppliedType(tc: Type, arg: Tree): Tree = tc match {
    case TypeRef(_, sym, ps)                    =>
      tq"$sym[..$ps, $arg]"
    case PolyType(List(p), TypeRef(_, sym, ps)) =>
      val fixedPs = ps.map {
        case pp if pp.typeSymbol == p => arg
        case other                    => tq"$other"
      }
      tq"$sym[..$fixedPs]"
    case PolyType(_, _)                         =>
      c.error(c.enclosingPosition, "Attempt to derive on polytype with more than 1 argument")
      tq"$tc"
    case _                                      =>
      tq"$tc[$arg]"
  }

  private def nameAndTypes(obj: Tree): NameAndTypes = {
    val mangledName = obj.toString.replaceAll("[^\\w]", "_")
    val name        = c.freshName(mangledName)

    val objTyp = c.typecheck(obj).tpe

    val nt = objTyp match {
      case IsSpecificDerivation(f, t, nt, d) => new NameAndTypes(name, f, t, nt, d, cascade = true)
      case IsDerivation(f, t, nt, d)         => new NameAndTypes(name, f, t, nt, d, cascade = true)
      case HKDerivation(f, t, nt, d)         =>
        objTyp match {
          case ParamRequire(fr, _, _, _) => new NameAndTypes(name, fr, t, nt, d, cascade = true)
          case _                         => new NameAndTypes(name, f, t, nt, d, cascade = false)
        }

      case _ => abort(s"$obj seems not extending InstanceDef traits")
    }

    val passArgs = objTyp.baseType(PassTypeArgsSymbol) match {
      case TypeRef(_, _, _) => true
      case _                => false
    }

    val keepRefinements = objTyp.baseType(KeepRefinementsSymbol) match {
      case TypeRef(_, _, _) => true
      case _                => false
    }

    nt.copy(passArgs = passArgs, keepRefinements = keepRefinements)
  }

  trait DerivationMatcher {
    def unapply(objType: Type): Option[(Type, Type, Type, Int)]
  }

  class DerivationList(ds: IsInstanceDef*) extends DerivationMatcher {
    def unapply(objType: Type): Option[(Type, Type, Type, Int)] =
      ds.iterator.flatMap(_.unapply(objType)).collectFirst { case x => x }
  }

  class IsInstanceDef(t: Type, drop: Int) extends DerivationMatcher {
    val constrSymbol                                            = t.typeConstructor.typeSymbol
    def unapply(objType: Type): Option[(Type, Type, Type, Int)] =
      objType.baseType(constrSymbol) match {
        case TypeRef(_, _, List(tc))           => Some((tc, tc, tc, drop))
        case TypeRef(_, _, List(from, to, nt)) => Some((from, to, nt, drop))
        case _                                 => None
      }
  }

  def isInstanceDef[T: TypeTag](dropTParams: Int = 0) = new IsInstanceDef(typeOf[T], dropTParams)
  def hkInstanceDef[T: TypeTag](dropTParams: Int = 0) = new IsInstanceDef(typeOf[T], dropTParams)

  private def debug[S](s: S, pref: String = ""): S = {
    c.info(
      c.enclosingPosition,
      pref + " " + (s match {
        case null => "null"
        case _    => s.toString
      }),
      false
    )
    s
  }
  private def abort(s: String) = c.abort(c.enclosingPosition, s)
}

object Derevo {
  private[Derevo] sealed trait NewtypeP[tree] {
    def underlying: tree
  }
  private[Derevo] final case class NewtypeCls[tree](underlying: tree)            extends NewtypeP[tree]
  private[Derevo] final case class NewtypeMod[tree](underlying: tree, res: tree) extends NewtypeP[tree]

  private[Derevo] final case class NameAndTypesP[typ](
      name: String,
      from: typ,
      to: typ,
      newtype: typ,
      drop: Int,
      cascade: Boolean,
      keepRefinements: Boolean = false,
      passArgs: Boolean = false,
  )
}
