package sai.structure.freer3

import scala.language.{higherKinds, implicitConversions}

import Eff._
import Freer._
import Handlers._
import OpenUnion._
import State._
import IO._

object Knapsack {
  import Nondet._
  // (12,List(List(3), List(2, 1), List(1, 2), List(1, 1, 1)))
  // List(List((1,List(3))), List((2,List(2, 1))), List((2,List(1, 2)), (3,List(1, 1, 1))))

  // import NondetList._
  // val Nondet = NondetList

  def runLocalState[R <: Eff, S, A](s: S, comp: Comp[State[S, *] ⊗ (Nondet ⊗ R), A]): Comp[R, List[Comp[R, List[(S, A)]]]] = {
    val p: Comp[Nondet ⊗ R, S => Comp[Nondet ⊗ R, (S, A)]] = State.run[Nondet ⊗ R, S, A](comp)
    val p1: Comp[R, List[S => Comp[Nondet ⊗ R, (S, A)]]] = Nondet.run[R, S => Comp[Nondet ⊗ R, (S, A)]](p)
    for {fs <- p1} yield fs.map(f => Nondet.run(f(s)))
  }

  def inc[E <: Eff](implicit I: State[Int, *] ∈ E): Comp[E, Unit] =
    for {
      x <- get
      _ <- put(x + 1)
    } yield ()

  def select_inc[R <: Eff, A](xs: List[A])
    (implicit I1: Nondet ∈ R, I2: State[Int, *] ∈ R, I3: IO ∈ R): Comp[R, A] =
    xs.map(Return[R, A]).foldRight[Comp[R, A]](fail) {
      case (a, b) =>
        for {
          _ <- inc
          x <- choice(a, b)
          // _ <- writeStr("select " + x.toString)
        } yield x
    }
  /*
    for {
      x <- select(xs)
      _ <- inc[R]
    } yield x
     */

  def knapsack[R <: Eff](w: Int, vs: List[Int])
    (implicit I1: Nondet ∈ R, I2: State[Int, *] ∈ R, I3: IO ∈ R): Comp[R, List[Int]] = {
    if (w < 0) fail
    else if (w == 0)
      ret(List())
    else for {
      v <- select_inc(vs)
      xs <- knapsack(w - v, vs)
    } yield v :: xs
  }

  def runGlobalKnapsack = {
    val p: Comp[Nondet ⊗ (IO ⊗ (State[Int, *] ⊗ ∅)), List[Int]] =
      knapsack[Nondet ⊗ (IO ⊗ (State[Int, *] ⊗ ∅))](3, List(3, 2, 1))
    val p1: Comp[IO ⊗ (State[Int, *] ⊗ ∅), List[List[Int]]] = Nondet.run[IO ⊗ (State[Int, *] ⊗ ∅), List[Int]](p)
    val p2: Comp[State[Int, *] ⊗ ∅, List[List[Int]]] = IO.run[State[Int, *] ⊗ ∅, List[List[Int]]](p1)
    val p3: Comp[∅, Int => Comp[∅, (Int, List[List[Int]])]] = State.run[∅, Int, List[List[Int]]](p2)
    println(extract(extract(p3)(0)))
  }

  def runLocalKnapsack = {
    val p: Comp[IO ⊗ (State[Int, *] ⊗ (Nondet ⊗ ∅)), List[Int]] =
      knapsack[IO ⊗ (State[Int, *] ⊗ (Nondet ⊗ ∅))](3, List(3, 2, 1))
    val p1: Comp[State[Int, *] ⊗ (Nondet ⊗ ∅), List[Int]] =
      IO.run(p)
    val p2: Comp[∅, List[Comp[∅, List[(Int, List[Int])]]]] = runLocalState(0, p1)
    println(extract(p2).map(extract))
    /* Equivelent to the followings:
    val p2: Comp[Nondet ⊗ ∅, Int => Comp[Nondet ⊗ ∅, (Int, List[Int])]] =
      State.run[Nondet ⊗ ∅, Int, List[Int]](p1)
    val p3: Comp[∅, List[Int => Comp[Nondet ⊗ ∅, (Int, List[Int])]]] =
      Nondet.run[∅, Int => Comp[Nondet ⊗ ∅, (Int, List[Int])]](p2)
    val p3v: List[Int => Comp[Nondet ⊗ ∅, (Int, List[Int])]] = extract(p3)
    println(p3v.map(f => extract(Nondet.run(f(0)))))
     */
    //List(List((1,List(3)), (5,List(2, 1)), (5,List(1, 2)), (9,List(1, 1, 1))))
  }

  def main(args: Array[String]) {
    //def run[E <: Eff, A]: Comp[Nondet ⊗ E, A] => Comp[E, List[A]] =
    //def run[E <: Eff, S, A]: Comp[State[S, *] ⊗ E, A] => Comp[E, S => Comp[E, (S, A)]] =
    runGlobalKnapsack
    runLocalKnapsack
  }
}

object FreerUnstagedImp {
  import Nondet._
  import Knapsack._
  import sai.lang.ImpLang._

  trait Value
  case class IntV(i: Int) extends Value
  case class BoolV(b: Boolean) extends Value
  case class SymV(e: Expr) extends Value

  type PC = Set[Expr]
  type Store = Map[String, Value]
  type SS = (Store, PC)

  def getStore[R <: Eff](implicit I: State[SS, *] ∈ R): Comp[R, Store] =
    for {s <- get} yield s._1
  def getPC[R <: Eff](implicit I: State[SS, *] ∈ R): Comp[R, PC] =
    for {s <- get} yield s._2
  def updateStore[R <: Eff](x: String, v: Value)(implicit I: State[SS, *] ∈ R): Comp[R, Unit] =
    for {s <- get; _ <- put((s._1 + (x -> v), s._2))} yield ()
  def updatePathCond[R <: Eff](e: Expr)(implicit I: State[SS, *] ∈ R): Comp[R, Unit] =
    for {s <- get; _ <- put((s._1, Set(e) ++ s._2))} yield ()

  def eval(e: Expr, σ: Store): Value =
    e match {
      case Lit(i: Int) => IntV(i)
      case Lit(b: Boolean) => BoolV(b)
      case Var(x) => σ(x)
      case Op1("-", e) =>
        eval(e, σ) match {
          case IntV(i) => IntV(-i)
          case SymV(e) => SymV(Op1("-", e))
        }
      case Op2(op, e1, e2) =>
        val v1 = eval(e1, σ)
        val v2 = eval(e2, σ)
          (op, v1, v2) match {
          case ("+", IntV(i1), IntV(i2)) => IntV(i1 + i2)
          case ("-", IntV(i1), IntV(i2)) => IntV(i1 - i2)
          case ("*", IntV(i1), IntV(i2)) => IntV(i1 * i2)
          case ("/", IntV(i1), IntV(i2)) => IntV(i1 / i2)
          case ("==", IntV(i1), IntV(i2)) => BoolV(i1 == i2)
          case ("<=", IntV(i1), IntV(i2)) => BoolV(i1 <= i2)
          case (">=", IntV(i1), IntV(i2)) => BoolV(i1 >= i2)
          case ("<", IntV(i1), IntV(i2)) => BoolV(i1 < i2)
          case (">", IntV(i1), IntV(i2)) => BoolV(i1 > i2)
          case (op, SymV(e1), SymV(e2)) => SymV(Op2(op, e1, e2))
        }
    }

  def eval[R <: Eff](e: Expr)(implicit I1: State[SS, *] ∈ R, I2: IO ∈ R): Comp[R, Value] =
    e match {
      case Input() => for { i <- readInt } yield IntV(i)
      case _ => for { σ <- getStore } yield eval(e, σ)
    }

  def exec[R <: Eff](s: Stmt)(implicit I1: State[SS, *] ∈ R, I2: IO ∈ R, I3: Nondet ∈ R): Comp[R, Unit] =
    s match {
      case Skip() => ret(())
      case Assign(x, e) => for { v <- eval(e); σ <- getStore; _ <- updateStore(x, v) } yield ()
      case Cond(e, s1, s2) =>
        for {
          v <- eval(e)
          _ <- if (v == BoolV(true)) exec(s1)
          else if (v == BoolV(false)) exec(s2)
          else choice(exec(s1), exec(s2))
        } yield ()
      case Seq(s1, s2) => for {_ <- exec(s1); _ <- exec(s2)} yield ()
      case While(e, s) =>
        /*
         for {
         v <- eval(e)
         _ <- if (v == BoolV(true)) exec(Seq(s, While(e, s)))
         else if (v == BoolV(false)) exec(Skip())
         else choice(exec(Seq(s, While(e, s))), exec(Skip()))
         } yield ()
         */
        // TODO: define an "unroll" effect?
        def loop: Comp[R, Unit] = for {
          v <- eval(e)
          _ <- if (v == BoolV(true)) for { _ <- exec(s); _ <- loop } yield ()
          else if (v == BoolV(false)) exec(Skip())
          else choice(for { _ <- exec(s); _ <- loop } yield (), exec(Skip()))
        } yield ()
        loop
      case Output(e) => for {
        v <- eval(e)
        _ <- writeStr(v.toString)
      } yield ()
    }

  def run(s: Stmt) = {
    val ss0: SS = (Map(), Set())
    val p: Comp[IO ⊗ (State[SS, *] ⊗ (Nondet ⊗ ∅)), Unit] =
      exec[IO ⊗ (State[SS, *] ⊗ (Nondet ⊗ ∅))](s)
    val p1: Comp[State[SS, *] ⊗ (Nondet ⊗ ∅), Unit] = IO.run(p)
    val p2: Comp[∅, List[Comp[∅, List[(SS, Unit)]]]] = runLocalState(ss0, p1)
    println(extract(p2).map(extract))
  }
}
