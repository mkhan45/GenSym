package sai.llsc

import lms.core._
import lms.core.Backend._
import lms.core.virtualize
import lms.macros.SourceContext
import lms.core.stub.{While => _, _}

import sai.lang.llvm._
import sai.lang.llvm.IR._
import sai.lang.llvm.parser.Parser._

import sai.lmsx._
import sai.utils.Utils.time
import sai.lang.llvm.Benchmarks._
import sai.lang.llvm.OOPSLA20Benchmarks._
import sai.lang.llvm.TestComp.ArrayExamples._
import sai.lang.llvm.TestComp.ArrayPrograms._

import sys.process._

import org.scalatest.FunSuite

import Config._

class Optimization extends TestLLSC {
  import scala.collection.mutable.ListBuffer
  import java.io.{File, FileWriter}
  val writer = new FileWriter(new File("opt_exp.csv"), true)
  val N = 10

  def testLLSC(N: Int, llsc: LLSC, tst: TestPrg): Unit = {
    val TestPrg(m, name, f, config, cliArgOpt, exp) = tst
    test(llsc.insName + "_" + name) {
      val (code, t) = time {
        val code = llsc.newInstance(m, llsc.insName + "_" + name, f, config)
        code.genAll
        code
      }
      System.out.println(s"Generated ${llsc.insName} - $name - $t")
      val mkRet = code.make(4)
      assert(mkRet == 0, "make failed")
      for (i <- 1 to N) {
        Thread.sleep(1 * 1000)
        val prefix = "numactl -N1 -m1"
        val (output, ret) = code.runWithStatus(cliArgOpt.getOrElse(""), prefix)
        val resStat = parseOutput(llsc.insName, name, output)
        System.out.println(resStat)
        writer.append(s"$resStat\n")
        writer.flush()
      }
    }
  }
}

/*
class TestPureCPSNoOpt extends Optimization {
  val llsc = new PureCPSLLSC
  Config.disableOpt

  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/mergesort.ll"), "mergeSort_NoOpt", "@main", noArg, None, nPath(5040)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/bubblesort.ll"), "bubbleSort_NoOpt", "@main", noArg, None, nPath(720)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/knapsack.ll"), "knapsack_NoOpt", "@main", noArg, None, nPath(1666)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/kmpmatcher.ll"), "kmp_NoOpt", "@main", noArg, None, nPath(4181)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/nqueen.ll"), "nqueen_NoOpt", "@main", noArg, None, nPath(1363)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/quicksort.ll"), "quicksort_NoOpt", "@main", noArg, None, nPath(5040)))
}

class TestPureCPSOpt extends Optimization {
  val llsc = new PureCPSLLSC
  Config.enableOpt

  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/mergesort.ll"), "mergeSort_Opt", "@main", noArg, None, nPath(5040)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/bubblesort.ll"), "bubbleSort_Opt", "@main", noArg, None, nPath(720)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/knapsack.ll"), "knapsack_Opt", "@main", noArg, None, nPath(1666)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/kmpmatcher.ll"), "kmp_Opt", "@main", noArg, None, nPath(4181)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/nqueen.ll"), "nqueen_Opt", "@main", noArg, None, nPath(1363)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/quicksort.ll"), "quicksort_Opt", "@main", noArg, None, nPath(5040)))
}


class TestImpCPSOpt extends Optimization {
  val llsc = new ImpCPSLLSC
  Config.enableOpt
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/mergesort.ll"), "mergeSort_Opt", "@main", noArg, None, nPath(5040)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/bubblesort.ll"), "bubbleSort_Opt", "@main", noArg, None, nPath(720)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/knapsack.ll"), "knapsack_Opt", "@main", noArg, None, nPath(1666)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/kmpmatcher.ll"), "kmp_Opt", "@main", noArg, None, nPath(4181)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/nqueen.ll"), "nqueen_Opt", "@main", noArg, None, nPath(1363)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/quicksort.ll"), "quicksort_Opt", "@main", noArg, None, nPath(5040)))
}

class TestImpCPSNoOpt extends Optimization {
  val llsc = new ImpCPSLLSC
  Config.disableOpt
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/mergesort.ll"), "mergeSort_NoOpt", "@main", noArg, None, nPath(5040)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/bubblesort.ll"), "bubbleSort_NoOpt", "@main", noArg, None, nPath(720)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/knapsack.ll"), "knapsack_NoOpt", "@main", noArg, None, nPath(1666)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/kmpmatcher.ll"), "kmp_NoOpt", "@main", noArg, None, nPath(4181)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/nqueen.ll"), "nqueen_NoOpt", "@main", noArg, None, nPath(1363)))
  testLLSC(N, llsc, TestPrg(parseFile("benchmarks/opt_experiments/quicksort.ll"), "quicksort_NoOpt", "@main", noArg, None, nPath(5040)))
}
*/
