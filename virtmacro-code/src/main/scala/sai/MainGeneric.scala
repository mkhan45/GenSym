package sai

import scala.virtualization.lms.internal.GenericNestedCodegen
import scala.virtualization.lms.common.{
  SetOps => _, SetOpsExp => _, ScalaGenSetOps => _,
  ListOps => _, ListOpsExp => _, ScalaGenListOps => _,
  _}
import org.scala_lang.virtualized.virtualize
import org.scala_lang.virtualized.SourceContext

import sai.lms._
import sai.monads._
import sai.lattices._
import sai.lattices.Lattices._
import sai.examples._

trait MonadOps[R[_], M[_], A] {
  def map[B](f: R[A] => R[B]): M[B]
  def flatMap[B](f: R[A] => M[B]): M[B]
  def filter(f: R[A] => Boolean): M[A]
  def withFilter(f: R[A] => Boolean): M[A]
}

trait Semantics {
  import PCFLang._
  type R[_]
  type Ident = String
  type Addr
  type Value
  type Env = Map[Ident, Addr]
  type Store = Map[Addr, Value]
  type AnsM[T] <: MonadOps[R, AnsM, T]
  type Ans = AnsM[Value]

  type EvalFun = Expr => Ans

  // Environment operations
  def ask_env: AnsM[Env]
  def ext_env(x: String, a: R[Addr]): AnsM[Env]
  def local_env(ev: EvalFun)(e: Expr, ρ: R[Env]): AnsM[Value]

  // allocate addresses
  def alloc(x: String): AnsM[Addr]
  def alloc(σ: R[Store], x: String): R[Addr]

  // Store operations
  def get_store: AnsM[Store]
  def put_store(σ: R[Store]): AnsM[Unit]
  def update_store(a: R[Addr], v: R[Value]): AnsM[Unit]

  // Primitive operations
  def num(i: Int): Ans
  def get(ρ: R[Env], σ: R[Store], x: String): R[Value]
  def br0(test: R[Value], thn: => Ans, els: => Ans): Ans
  def prim(op: Symbol, v1: R[Value], v2: R[Value]): R[Value]
  def close(ev: EvalFun)(λ: Lam, ρ: R[Env]): R[Value]
  def ap_clo(ev: EvalFun)(fun: R[Value], arg: R[Value]): Ans

  def eval(ev: EvalFun)(e: Expr): Ans = e match {
    case Lit(i) => num(i)
    case Var(x) => for {
      ρ <- ask_env
      σ <- get_store
    } yield get(ρ, σ, x)
    case Lam(x, e) => for {
      ρ <- ask_env
    } yield close(ev)(Lam(x, e), ρ)
    case App(e1, e2) => for {
      v1 <- ev(e1)
      v2 <- ev(e2)
      rt <- ap_clo(ev)(v1, v2)
    } yield rt
    case Let(x, rhs, e) => for {
      v <- ev(rhs)
      α <- alloc(x)
      _ <- update_store(α, v)
      ρ <- ext_env(x, α)
      rt <- local_env(ev)(e, ρ)
    } yield rt
    case If0(e1, e2, e3) => for {
      cnd <- ev(e1)
      rt <- br0(cnd, ev(e2), ev(e3))
    } yield rt
    case Aop(op, e1, e2) => for {
      v1 <- ev(e1)
      v2 <- ev(e2)
    } yield prim(op, v1, v2)
    case Rec(x, rhs, e) => for {
      α <- alloc(x)
      ρ <- ext_env(x, α)
      v <- local_env(ev)(rhs, ρ)
      _ <- update_store(α, v)
      rt <- local_env(ev)(e, ρ)
    } yield rt
  }

  val ρ0: R[Env]
  val σ0: R[Store]

  type Result
  def run(e: Expr): Result
}
