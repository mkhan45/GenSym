package sai.direct.core.ai

import scala.util.continuations._
import scala.language.implicitConversions
import scala.language.higherKinds

import sai.utils._
import sai.common.ai._
import sai.common.ai.Lattices.{NoRep => _, _}
import sai.direct.core.parser._

import sai.common._
import scala.lms.tutorial._
import scala.reflect.SourceContext
import scala.lms.internal.GenericNestedCodegen
import scala.lms.common.{SetOpsExp ⇒ _, ScalaGenSetOps ⇒ _, ListOps ⇒ _, ListOpsExp ⇒ _, ScalaGenListOps ⇒ _, _}

object Semantics {
  type NoRep[+T] = T

  trait EnvSpec[R[+_],K,V,E] { //TODO: can this also be F-bound poly? <: EnvSpec[R,K,V,E]
    def get(m: R[E], k: R[K]): R[V]
    def +(m: R[E], kv: (R[K], R[V])): R[E]
  }
  object EnvSpec {
    def apply[R[+_],K,V,E](implicit ρ: EnvSpec[R,K,V,E]): EnvSpec[R,K,V,E] = ρ
  }
  implicit class EnvOps[R[+_],K,V,E:({type λ[ε] = EnvSpec[R,K,V,ε]})#λ](ρ: R[E]) {
    def apply(k: R[K]): R[V] = EnvSpec[R,K,V,E].get(ρ, k)
    def +(kv: (R[K], R[V])): R[E] = EnvSpec[R,K,V,E].+(ρ, kv)
  }

  trait StoreSpec[R[+_], K, V, S] {  //<: StoreSpec[R,K,V,S]] {
    def get(s: R[S], k: R[K]): R[V]
    def getOrElse(s: R[S], k: R[K], dft: R[V]): R[V]
    def update(s: R[S], k: R[K], v: R[V]): R[S]
    def contains(s: R[S], k: R[K]): R[Boolean]
    def ⊔(s1: R[S], s2: R[S]): R[S]
  }
  object StoreSpec {
    def apply[R[+_],K,V,S](implicit σ: StoreSpec[R,K,V,S]): StoreSpec[R,K,V,S] = σ
  }
  implicit class StoreOps[R[+_],K,V,S:({type λ[σ] = StoreSpec[R,K,V,σ]})#λ](σ: R[S]) {
    def apply(k: R[K]): R[V] = StoreSpec[R,K,V,S].get(σ, k)
    def getOrElse(k: R[K], dft: R[V]): R[V] = StoreSpec[R,K,V,S].getOrElse(σ, k, dft)
    def +(kv: (R[K], R[V])): R[S] = StoreSpec[R,K,V,S].update(σ, kv._1, kv._2)
    def update(kv: (R[K], R[V])): R[S] = StoreSpec[R,K,V,S].update(σ, kv._1, kv._2)
    def update(k: R[K], v: R[V]): R[S] = StoreSpec[R,K,V,S].update(σ, k, v)
    def contains(k: R[K]): R[Boolean] = StoreSpec[R,K,V,S].contains(σ, k)
    def ⊔(that: R[S]): R[S] = StoreSpec[R,K,V,S].⊔(σ, that)
  }

  trait CacheSpec[R[+_], K, V, C] { // <: CacheSpec[R,K,V,C]
    def inGet(c: R[C], k: R[K]): R[V]
    def outGet(c: R[C], k: R[K]): R[V]
    def inContains(c: R[C], k: R[K]): R[Boolean]
    def outContains(c: R[C], k: R[K]): R[Boolean]
    def outUpdate(c: R[C], k: R[K], v: R[V]): R[C]
    def ⊔(c1: R[C], c2: R[C]): R[C]
  }
  object CacheSpec {
    def apply[R[+_],K,V,C](implicit c: CacheSpec[R,K,V,C]): CacheSpec[R,K,V,C] = c
  }
  implicit class CacheOps[R[+_],K,V,C:({type λ[c] = CacheSpec[R,K,V,c]})#λ](c: R[C]) {
    def inGet(k: R[K]): R[V] = CacheSpec[R,K,V,C].inGet(c, k)
    def inContains(k: R[K]): R[Boolean] = CacheSpec[R,K,V,C].inContains(c, k)
    def outGet(k: R[K]): R[V] = CacheSpec[R,K,V,C].outGet(c, k)
    def outContains(k: R[K]): R[Boolean] = CacheSpec[R,K,V,C].outContains(c, k)
    def outUpdate(k: R[K], v: R[V]): R[C] = CacheSpec[R,K,V,C].outUpdate(c, k, v)
    def outUpdateFromIn(c: R[C], k: R[K]): R[C] = CacheSpec[R,K,V,C].outUpdate(c, k, CacheSpec[R,K,V,C].inGet(c, k))
    def ⊔(c1: R[C], c2: R[C]): R[C] = CacheSpec[R,K,V,C].⊔(c1, c2)
  }

  trait Sem {
    type R[+T]
    type Addr
    type Ident
    type Value
    type Env
    implicit val envImp: EnvSpec[R, Ident, Addr, Env]
    type Store
    implicit val storeImp: StoreSpec[R, Addr, Value, Store]
    type Ans
    //type Contour

    val ρ0: R[Env]
    val σ0: R[Store]
    def alloc(x: R[Ident], σ: R[Store]): R[Addr]

    type EvalFun = (Expr, R[Env], R[Store]) ⇒ Ans
    def eval(ev: EvalFun)(e: Expr, ρ: R[Env], σ: R[Store]): Ans
    def fix(ev: EvalFun => EvalFun): EvalFun = (e, ρ, σ) => ev(fix(ev))(e, ρ, σ)
    def eval_top(e: Expr): Ans = fix(eval)(e, ρ0, σ0)
  }

  trait Concrete extends Sem {
    type Ident = String
    type Addr = Int
    type Env = Map[Ident, Addr]
    type Store = Map[Addr, Value]
    abstract class Value
    type Ans = (R[Value], R[Store])
    //case class NumV(i: Int) extends Value
    //case class CloV(λ: Lam, ρ: Env) extends Value //TODO: where put this?
  }

  trait Abstract extends Sem {
    type Ident = String
    case class Addr(x: Ident)
    type Env = Map[Ident, Addr]
    type Store = Map[Addr, Value]
    //type Contour = List[Expr]
    abstract class AbsValue
    case class CloV(λ: Lam, ρ: Env) extends AbsValue
    case class NumV() extends AbsValue
    type Value = Set[AbsValue]
    type Ans = (R[Value], R[Store])
  }

  //TODO: Make sure this is path-sensitive (not flow-sensitive).
  trait PathSenAbstract extends Sem {
    type Ident = String
    case class Addr(x: Ident)
    type Env = Map[Ident, Addr]
    type Store = Map[Addr, Value]
    //type Contour = List[Expr]
    abstract class AbsValue
    case class CloV(λ: Lam, ρ: Env) extends AbsValue
    case class NumV() extends AbsValue
    type Value = Set[AbsValue]
    type Ans = R[Set[(Value, Store)]]
  }

}

import Semantics._

object UnStaged {

  /*************************** CONCRETE ****************************/

  object NoRepConSem extends Concrete {
    case class NumV(i: Int) extends Value
    case class CloV(λ: Lam, ρ: Env) extends Value

    type R[+T] = NoRep[T]
    implicit val envImp: EnvSpec[R,Ident,Addr,Env] = new EnvSpec[R,Ident,Addr,Env] {
      def get(m: Env, k: Ident): Addr = m(k)
      def +(m: Env, kv: (Ident, Addr)): Env = m + (kv._1 -> kv._2)
    }
    val ρ0: Env = Map[Ident,Addr]()

    implicit val storeImp: StoreSpec[R,Addr,Value,Store] = new StoreSpec[R,Addr,Value,Store] {
      def get(map: Store, k: Addr): Value = map(k)
      def getOrElse(map: Store, k: Addr, dft: Value): Value = map.getOrElse(k, dft)
      def update(map: Store, k: Addr, v: Value): Store = map + (k → v)
      def contains(map: Store, k: Addr): Boolean = map.contains(k)
      def ⊔(s1: Store, s2: Store): Store = throw new RuntimeException("Not implemented")
    }
    val σ0: Store = Map[Addr,Value]()

    def evalArith(op: Symbol, e1v: NumV, e2v: NumV): NumV = op match {
      case '+ ⇒ NumV(e1v.i + e2v.i)
      case '- ⇒ NumV(e1v.i - e2v.i)
      case '* ⇒ NumV(e1v.i * e2v.i)
    }

    def alloc(x: Ident, σ: Store): Addr = σ.size + 1
    def eval(ev: EvalFun)(e: Expr, ρ: Env, σ: Store): Ans = e match {
      case Lit(i) => (NumV(i), σ)
      case Var(x) => (σ(ρ(x)), σ)
      case Lam(x, e) => (CloV(Lam(x,e), ρ), σ)
      case App(e1, e2) =>
        val (clo: CloV, e1σ) = ev(e1, ρ, σ)
        val (e2v, e2σ) = ev(e2, ρ, e1σ)
        val α = alloc(clo.λ.x, e2σ)
        ev(clo.λ.body, clo.ρ + (clo.λ.x → α), e2σ + (α → e2v))
      case Rec(x, f, body) =>
        val α = alloc(x, σ)
        val ρ_* = ρ + (x → α)
        val (fv, fσ) = ev(f, ρ_*, σ)
        val σ_* = fσ + (α → fv)
        ev(body, ρ_*, σ_*)
      case Let(x, e, body) => ev(App(Lam(x, body), e), ρ, σ)
      case If0(cnd, thn, els) =>
        val (cndv:NumV, cndσ) = ev(cnd, ρ, σ)
        if (cndv.i == 0) ev(thn, ρ, cndσ)
        else ev(els, ρ, cndσ)
      case AOp(op, e1, e2) =>
        val (e1v:NumV, e1σ) = ev(e1, ρ, σ)
        val (e2v:NumV, e2σ) = ev(e2, ρ, e1σ)
        (evalArith(op, e1v, e2v), e2σ)
    }
  }

  /*************************** ABSTRACT ****************************/

  object NoRepAbsSem extends Abstract {
    type R[+T] = NoRep[T]

    implicit val envImp: EnvSpec[R,Ident,Addr,Env] = new EnvSpec[R,Ident,Addr,Env] {
      def get(m: Env, k: Ident): Addr = m(k)
      def +(m: Env, kv: (Ident, Addr)): Env = m + (kv._1 -> kv._2)
    }
    val ρ0: Env = Map[Ident,Addr]()

    implicit val storeImp: StoreSpec[R,Addr,Value,Store] = new StoreSpec[R,Addr,Value,Store] {
      def get(map: Store, k: Addr): Value = map(k)
      def getOrElse(map: Store, k: Addr, dft: Value): Value = map.getOrElse(k, dft)
      def update(map: Store, k: Addr, v: Value): Store =
        map + (k → (map.getOrElse(k, v.bot) ⊔ v))
      def contains(map: Store, k: Addr): Boolean = map.contains(k)
      def ⊔(s1: Store, s2: Store): Store = s1 ⊔ s2
    }
    val σ0: Store = Map[Addr,Value]()

    def alloc(x: Ident, σ: Store): Addr = Addr(x)
    def eval(ev: EvalFun)(e: Expr, ρ: Env, σ: Store): Ans = e match {
      case Var(x) ⇒ (σ(ρ(x)), σ)
      case Lam(x, e) ⇒ (Set(CloV(Lam(x,e), ρ)), σ)
      case App(e1, e2) ⇒
        val (e1vs, e1σ) = ev(e1, ρ, σ)
        val (e2vs, e2σ) = ev(e2, ρ, e1σ)
        val result = for (CloV(Lam(x,e), λρ) <- e1vs; e2v <- e2vs) yield {
          val α = alloc(x, e2σ)
          val ρ_* = λρ + (x → α)
          val σ_* = e2σ + (α → Set(e2v))
          ev(e, ρ_*, σ_*)
        }
        result.reduceLeft[Ans] { case ((v1,s1), (v2,s2)) => (v1++v2, s1⊔s2) }
    }
  }

  object NoRepAbsSem2 extends PathSenAbstract {
    type R[+T] = NoRep[T]
    implicit val envImp: EnvSpec[R,Ident,Addr,Env] = new EnvSpec[R,Ident,Addr,Env] {
      def get(m: Env, k: Ident): Addr = m(k)
      def +(m: Env, kv: (Ident, Addr)): Env = m + (kv._1 -> kv._2)
    }
    val ρ0: Env = Map[Ident,Addr]()
    implicit val storeImp: StoreSpec[R,Addr,Value,Store] = new StoreSpec[R,Addr,Value,Store] {
      def get(map: Store, k: Addr): Value = map(k)
      def getOrElse(map: Store, k: Addr, dft: Value): Value = map.getOrElse(k, dft)
      def update(map: Store, k: Addr, v: Value): Store =
        map + (k → (map.getOrElse(k, v.bot) ⊔ v))
      def contains(map: Store, k: Addr): Boolean = map.contains(k)
      def ⊔(s1: Store, s2: Store): Store = s1 ⊔ s2
    }
    val σ0: Store = Map[Addr,Value]()

    def alloc(x: Ident, σ: Store): Addr = Addr(x)
    def eval(ev: EvalFun)(e: Expr, ρ: Env, σ: Store): Ans = e match {
      case Lit(i) => Set((Set(NumV()), σ))
      case Var(x) => Set((σ(ρ(x)), σ))
      case Lam(x, e) => Set((Set(CloV(Lam(x,e), ρ)), σ))
      case App(e1, e2) =>
        val result: Set[Ans] =
          for ((e1vs, e1σ) <- ev(e1, ρ, σ);
               CloV(Lam(x, e), λρ) <- e1vs;
               (e2vs, e2σ) <- ev(e2, ρ, e1σ)) yield {
            val α = alloc(x, e2σ)
            val ρ_* = λρ + (x → α)
            val σ_* = e2σ + (α → e2vs)
            ev(e, ρ_*, σ_*)
          }
        result.flatten
    }


    type Config = (Expr, Env, Store)

    case class Cache(in: Map[Config, Ans], out: Map[Config, Ans]) { //extends CacheSpec[NoRep,Config,Ans,Cache] {
      def inGet(cfg: Config): Ans = in.getOrElse(cfg, Set())
      def inContains(cfg: Config): Boolean = in.contains(cfg)
      def outGet(cfg: Config):Ans = out.getOrElse(cfg, Set())
      def outContains(cfg: Config): Boolean = out.contains(cfg)
      def outUpdate(cfg: Config, vss: Ans): Cache = {
        val oldv: Ans = out.getOrElse(cfg, Set())
        Cache(in, out + (cfg -> (oldv ⊔ vss)))
      }
      def outUpdateFromIn(cfg: Config): Cache = outUpdate(cfg, inGet(cfg))
      def ⊔(that: Cache): Cache = Cache(in ⊔ that.in, out ⊔ that.out)
    }

    def eval_cache(ev: EvalFun)(e: Expr, ρ: Env, σ: Store, cache: Cache): (Ans, Cache) = {
      val config = (e, ρ, σ)
      if (cache.outContains(config)) {
        (cache.outGet(config), cache)
      } else {
        val cache_* = cache.outUpdateFromIn(config)
        val ans = ev(e, ρ, σ) //TODO:
        (ans, cache_*.outUpdate(config, ans))
      }
    }

    override def eval_top(e: Expr): Ans = ???
  }
}

trait LamOps extends Base with StringOps with Variables {
  implicit def repToLamOps(l: Rep[Lam]) = new LamOpsCls(l)
  class LamOpsCls(l: Rep[Lam]) {
    def x = lam_var(l)
    def body = lam_body(l)
  }
  def lam_var(l: Rep[Lam])(implicit pos: SourceContext): Rep[String]
  def lam_body(l: Rep[Lam])(implicit pos: SourceContext): Rep[Expr]
}

trait LamOpsExp extends BaseExp with LamOps with ListOpsExp with StringOpsExp with VariablesExp {
  implicit def LamTyp: Typ[Lam] = manifestTyp
  implicit def ExprTyp: Typ[Expr] = manifestTyp
  case class LamVar(l: Exp[Lam]) extends Def[String]
  case class LamBody(l: Exp[Lam]) extends Def[Expr]
  def lam_var(l: Exp[Lam])(implicit pos: SourceContext): Exp[String] = LamVar(l)
  def lam_body(l: Exp[Lam])(implicit pos: SourceContext): Exp[Expr] = LamBody(l)
}

trait CloVOps extends Base with Concrete with LamOps with MapOps with Variables {
  implicit def repToCloVOps(c: Rep[CloV]) = new CloVOpsCls(c)
  case class CloV(λ: Lam, ρ: Rep[Env]) extends Value
  /*
  object CloV {
    def apply(lam: Lam, env: Rep[Env])(implicit pos: SourceContext) = clov_new(lam, env)
  }
   */
  class CloVOpsCls(c: Rep[CloV]) {
    def λ: Rep[Lam] = clov_lam(c)
    def ρ: Rep[Env] = clov_env(c)
  }
  //def clov_new(λ: Lam, ρ: Rep[Env])(implicit pos: SourceContext): Rep[CloV]
  def clov_lam(c: Rep[CloV])(implicit pos: SourceContext): Rep[Lam]
  def clov_env(c: Rep[CloV])(implicit pos: SourceContext): Rep[Env]
}

trait CloVOpsExp extends BaseExp with CloVOps with LamOpsExp with Concrete with MapOpsExp with VariablesExp {
  implicit def CloVTyp: Typ[CloV] = manifestTyp

  //case class CloVNew(λ: Lam, ρ: Rep[Env]) extends Def[CloV]
  case class CloVLam(c: Exp[CloV]) extends Def[Lam]
  case class CloVEnv(c: Exp[CloV]) extends Def[Env]

  //def clov_new(λ: Lam, ρ: Rep[Env])(implicit pos: SourceContext): Exp[CloV] = Const(CloV(λ, ρ))
  def clov_lam(c: Exp[CloV])(implicit pos: SourceContext): Exp[Lam] = c match {
    case Const(CloV(λ, _)) => Const(λ)
    case _ => CloVLam(c)
  }
  def clov_env(c: Exp[CloV])(implicit pos: SourceContext): Exp[Env] = c match {
    case Const(CloV(_, ρ)) => ρ
    case _ => CloVEnv(c)
  }
}

trait NumVOps extends Base with Concrete with PrimitiveOps {
  implicit def repToNumVOps(n: Rep[NumV]) = new NumVOpsCls(n)
  case class NumV(i: Rep[Int]) extends Value
  class NumVOpsCls(n: Rep[NumV]) {
    def i: Rep[Int] = numv_i(n)
  }
  def numv_i(n: Rep[NumV])(implicit pos: SourceContext): Rep[Int]
}

trait NumVOpsExp extends BaseExp with Concrete with PrimitiveOpsExp with NumVOps {
  implicit def NumVTyp: Typ[NumV] = manifestTyp
  case class NumVI(n: Exp[NumV]) extends Def[Int]
  def numv_i(n: Exp[NumV])(implicit pos: SourceContext): Exp[Int] = NumVI(n)
}

trait ScalaGenConcInterp extends GenericNestedCodegen {
  val IR: LamOpsExp with CloVOpsExp with NumVOpsExp
  import IR._

  override def emitNode(sym: Sym[Any], rhs: Def[Any]) = rhs match {
    case LamVar(lam) => emitValDef(sym, src"$lam.vars")
    //case CloVNew(λ, ρ) => emitValDef(sym, src"CloV($λ, $ρ)")
    case CloVLam(c) => emitValDef(sym, src"$c.λ")
    case CloVEnv(c) => emitValDef(sym, src"$c.ρ")
    case _ => super.emitNode(sym, rhs)
  }
}

trait StagedInterp extends Dsl with Concrete
    with DslExp with MapOpsExp with SetOpsExp with StringOpsExp with UncheckedOpsExp
    with ListOpsExp with TupleOpsExp with TupledFunctionsRecursiveExp
    with LamOpsExp with CloVOpsExp with NumVOpsExp {
  def power(b: Rep[Int], x: Int): Rep[Int] =
      if (x == 0) 1 else b * power(b, x-1)

  /***************************************************/

  type R[+T] = Rep[T]
  implicit def AbsValueTyp: Typ[Value] = manifestTyp

  implicit val envImp: EnvSpec[Rep,Ident,Addr,Env] = new EnvSpec[Rep,Ident,Addr,Env] {
    def get(m: Rep[Env], k: Rep[Ident]): Rep[Addr] = m(k)
    def +(m: Rep[Env], kv: (Rep[Ident], Rep[Addr])): Rep[Env] = m + (kv._1 -> kv._2)
  }
  val ρ0: Rep[Env] = Map[Ident,Addr]()

  implicit val storeImp: StoreSpec[Rep,Addr,Value,Store] = new StoreSpec[Rep,Addr,Value,Store] {
    def get(map: Rep[Store], k: Rep[Addr]): Rep[Value] = map(k)
    def getOrElse(map: Rep[Store], k: Rep[Addr], dft: Rep[Value]): Rep[Value] = map.getOrElse(k, dft)
    def update(map: Rep[Store], k: Rep[Addr], v: Rep[Value]): Rep[Store] = map + (k → v)
    def contains(map: Rep[Store], k: Rep[Addr]): Rep[Boolean] = map.contains(k)
    def ⊔(s1: Rep[Store], s2: Rep[Store]): Rep[Store] = s1 ⊔ s2
  }

  def lift[T:Typ](x: T): Rep[T] = unit[T](x)
  def fall[T:Typ](c: Rep[T]): T = c match { case Const(x) => x }

  def evalArith(op: Symbol, v1: Rep[NumV], v2: Rep[NumV]): Rep[NumV] = op match {
    case '+ ⇒ lift(NumV(v1.i + v2.i))
    case '- ⇒ lift(NumV(v1.i - v2.i))
    case '* ⇒ lift(NumV(v1.i * v2.i))
  }

  val σ0: Rep[Store] = Map[Addr, Value]()
  def alloc(x: Rep[Ident], σ: Rep[Store]): Rep[Addr] = σ.size+1
  def eval(ev: EvalFun)(e: Expr, ρ: Rep[Env], σ: Rep[Store]): Ans = e match {
    case Lit(i) => (lift(NumV(i)), σ) //TODO: get rid of `lift`
    case Var(x) => (σ(ρ(x)), σ)
    case Lam(x, e) => (lift(CloV(Lam(x, e), ρ)), σ)
    case App(e1, e2) =>
      val (clo: Rep[CloV], e1σ) = ev(e1, ρ, σ)
      val (e2v, e2σ) = ev(e2, ρ, e1σ)
      clo.λ match {
        case Const(Lam(x, body)) =>
          //FIXME
          //val Lam(x, body) = fall[Lam](clo.λ)
          val α = alloc(x, e2σ)
          ev(body, clo.ρ + (clo.λ.x → α), e2σ + (α → e2v))
        case λ =>
          (unchecked(s"apply(${λ})"), σ)
      }
    case Rec(x, f, body) =>
      val α = alloc(x, σ)
      val ρ_* = ρ + (lift(x) → α)
      val (fv, fσ) = ev(f, ρ_*, σ)
      val σ_* = fσ + (α → fv)
      ev(body, ρ_*, σ_*)
    case Let(x, e, body) => ev(App(Lam(x, body), e), ρ, σ)
    case If0(cnd, thn, els) =>
      val (cndv: Rep[NumV], cndσ) = ev(cnd, ρ, σ)
      if (cndv.i == 0) ev(thn, ρ, σ)
      else ev(els, ρ, σ)
    case AOp(op, e1, e2) =>
      val (e1v: Rep[NumV], e1σ) = ev(e1, ρ, σ)
      val (e2v: Rep[NumV], e2σ) = ev(e1, ρ, e1σ)
      (evalArith(op, e1v, e2v), e2σ)
  }
}

abstract class StagedInterpDriver extends DslDriver[Unit, Unit] with StagedInterp { q =>
  override val codegen = new DslGen with ScalaGenSetOps with ScalaGenListOps
      with ScalaGenMapOps with ScalaGenConcInterp with MyScalaGenTupledFunctions
      with ScalaGenUncheckedOps
  {
    val IR: q.type = q
  }
}

object Fixpoint {
  def fib(x: Int): Int = if (x == 0) 0
                         else if (x < 2) 1
                         else fib(x-1) + fib(x-2)

  object Memo {
    var cache = List[(Int, Int)]()
    def apply(f: Int => Int): Int => Int = {
      def eval(xs: List[(Int, Int)], x: Int): Int = xs match {
        case Nil =>
          val r = f(x)
          cache = (x, r)::cache
          r
        case (a,b)::xs =>
          if (x == a) b else eval(xs, x)
      }
      x => eval(cache, x)
    }
  }
  def mfib = Memo(fib)

  def Fib(f: Int => Int)(x: Int): Int =
    if (x == 0) 0
    else if (x < 2) 1
    else f(x-1) + f(x-2)
  def fix(F: (Int=>Int)=>(Int=>Int))(x: Int): Int = F(fix(F))(x)
  def fixFib(x: Int): Int = fix(Fib)(x)

  case class MemoFix[A,B]() {
    var cache = List[(A, B)]()
    def fix(F: (A=>B)=>(A=>B))(xx: A): B = {
      def eval(xs: List[(A, B)], x: A): B = xs match {
        case Nil =>
          val r = F(ff)(x)
          cache = (x, r)::cache
          r
        case (a,b)::tl =>
          if (x == a) b
          else eval(tl, x)
      }
      def ff(x: A) = eval(cache, x)
      ff(xx)
    }
  }
  def mFixFib(x: Int): Int = MemoFix().fix(Fib)(x)

  //TODO: the co-inductive one

  def main(args: Array[String]) {
    println(Utils.time { fib(40) })
    println(Utils.time { mfib(40) })
    println(Utils.time { mfib(40) })
    println(Utils.time { fixFib(40) })
    println(Utils.time { mFixFib(40) })
  }
}

object SADI2 extends TutorialFunSuite {
  import UnStaged._
  val under = "not applicable"

  def specializePower(x: Int): DslDriver[Int, Int] =
    new DslDriver[Int, Int] with StagedInterp { q =>
      val IR: q.type = q
      def snippet(b: Rep[Int]): Rep[Int] = power(b, x)
    }

  def specializeConcrete(prog: Expr): DslDriver[Unit, Unit] =
    new StagedInterpDriver {
      def snippet(unit: Rep[Unit]): Rep[Unit] = {
        val ans = eval_top(prog)
        println(ans._1)
      }
    }

  def main(args: Array[String]) {
    val id = App(Lam("x", Var("x")), Lit(42))
    val omega = App(Lam("x", App(Var("x"), Var("x"))), Lam("x", App(Var("x"), Var("x"))))
    val fact = Lam("n",
                   If0(Var("n"),
                       Lit(1),
                       AOp('*, Var("n"), App(Var("fact"), AOp('-, Var("n"), Lit(1))))))
    val fact5 = Rec("fact", fact, App(Var("fact"), Lit(5)))
    val fact10 = Rec("fact", fact, App(Var("fact"), Lit(10)))
    val rec = Rec("id", Lam("x", Var("x")), Var("id"))
    //println(NoRepConSem.eval_top(fact5))
    //NoRepConSem.eval_top(omega) /* Stack overflow */
    //NoRepAbsSem.eval_top(omega) /* Stack overflow */

    val code = specializeConcrete(rec)
    println(code.code)
    //code.precompile
    //println(code.eval(()))

    val power5 = specializePower(5)
    println(power5.code)
  }
}
