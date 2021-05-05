package sai.llsc

import sai.lang.llvm._
import sai.lang.llvm.IR._
import sai.lang.llvm.parser.Parser._

import lms.core._
import lms.core.Backend._
import lms.core.utils.time
import lms.core.virtualize
import lms.macros.SourceContext
import lms.core.stub.{While => _, _}

import sai.lmsx._
import scala.collection.immutable.{List => StaticList}

abstract class LLSCDriver[A: Manifest, B: Manifest](appName: String, folder: String = ".")
    extends SAISnippet[A, B] with SAIOps with LLSCEngine { q =>

  import java.io.{File, PrintStream}

  val codegen = new SymStagedLLVMGen {
    val IR: q.type = q
    val codegenFolder = s"$folder/$appName/"
  }

  // Assuming the working directory only contains subdir "build"
  // TODO: what it is special about `build`
  // TODO: remove this to somewhere for utilities
  def createNewDir: Boolean = {
    val codegenFolderFile = new File(codegen.codegenFolder)
    if (!codegenFolderFile.exists()) codegenFolderFile.mkdir
    else {
      val entries = codegenFolderFile.list()
      entries.map(x => {
        if (x == "build") {
          val build_dir = new File(codegenFolderFile.getPath, x)
          build_dir.list.map(x => new File(build_dir.getPath, x).delete)
          build_dir.delete
        }
        else new File(codegenFolderFile.getPath, x).delete
      })
      codegenFolderFile.delete
      codegenFolderFile.mkdir
    }
  }

  def genSource: Unit = {
    val folderFile = new File(folder)
    if (!folderFile.exists()) folderFile.mkdir
    createNewDir
    val mainStream = new PrintStream(s"$folder/$appName/$appName.cpp")
    val statics = Adapter.emitCommon1(appName, codegen, mainStream)(manifest[A], manifest[B])(x => Unwrap(wrapper(Wrap[A](x))))
    mainStream.close
  }

  // TODO: export LD_LIBRARY_PATH=../stp/build/lib
  def genMakefile: Unit = {
    val out = new PrintStream(s"$folder/$appName/Makefile")
    val curDir = new File(".").getCanonicalPath
    val libraries = codegen.libraryFlags.mkString(" ")
    val includes = codegen.includePaths.map(s"-I $curDir/" + _).mkString(" ")
    val libraryPaths = codegen.libraryPaths.map(s"-L $curDir/" + _).mkString(" ")

    out.println(s"""|BUILD_DIR = build
    |SRC_DIR = .
    |SOURCES = $$(shell find $$(SRC_DIR)/ -name "*.cpp")
    |TARGET = $appName
    |OBJECTS = $$(SOURCES:$$(SRC_DIR)/%.cpp=$$(BUILD_DIR)/%.o)
    |CC = g++ -std=c++17 -O3
    |CXXFLAGS = $includes
    |LDFLAGS = $libraryPaths
    |LDLIBS = $libraries -lpthread
    |
    |default: $$(TARGET)
    |
    |.SECONDEXPANSION:
    |
    |$$(OBJECTS): $$$$(patsubst $$(BUILD_DIR)/%.o,$$(SRC_DIR)/%.cpp,$$$$@)
    |\tmkdir -p $$(@D)
    |\t$$(CC) -c -o $$@ $$< $$(CXXFLAGS)
    |
    |$$(TARGET): $$(OBJECTS)
    |\t$$(CC) -o $$@ $$^ $$(LDFLAGS) $$(LDLIBS)
    |
    |clean:
    |\t@rm main 2>/dev/null || true
    |\t@rm build -rf 2>/dev/null || true
    |
    |.PHONY: default clean
    |""".stripMargin)
    out.close
  }

  def genAll: Unit = {
    genSource
    genMakefile
  }
}

object TestStagedSymExec {
  @virtualize
  def specialize(m: Module, name: String, fname: String, nSym: Int): LLSCDriver[Int, Unit] =
    new LLSCDriver[Int, Unit](name, "./llsc_gen") {
      def snippet(u: Rep[Int]) = {
        val args: Rep[List[Value]] = SymV.makeSymVList(nSym)
        val res = exec(m, fname, args, StaticList[Module]())
        // query a single test
        // res.head._1.pc.toList.foreach(assert(_))
        // handle(query(lit(false)))
        println(res.size)
      }
    }
  
  def runLLSC(m: Module, name: String, fname: String, nSym: Int = 0) {
    val (_, t) = sai.utils.Utils.time {
      val code = specialize(m, name, fname, nSym)
      code.genAll
    }
    println(s"compiling $name, time $t ms")
  }

  def main(args: Array[String]): Unit = {
    val usage = """
    Usage: llsc <.ll-filepath> <app-name> <entrance-fun-name> [n-sym-var]
    """
    if (args.size < 3) {
      println(usage)
    } else {
      val filepath = args(0)
      val appName = args(1)
      val fun = args(2)
      val nSym = if (args.isDefinedAt(3)) args(3).toInt else 0
      runLLSC(parseFile(filepath), appName, fun, nSym)
    }

    //runLLSC(sai.llvm.Benchmarks.add, "add.cpp", "@add")
    //runLLSC(sai.llvm.OOPSLA20Benchmarks.mp1048576, "mp1m", "@f", 20)
    //runLLSC(sai.llvm.Benchmarks.arrayAccess, "arrAccess", "@main")
    //runLLSC(sai.llvm.LLSCExpr.structReturnLong, "structR1", "@main")
    //runLLSC(sai.llvm.Coreutils.echo, "echo", "@main")
    //runLLSC(sai.llvm.LLSCExpr.complexStruct, "complexStruct", "@main")
    //testFunGen(sai.llvm.LLSCExpr.complexStruct, "complexStruct", "@main")
    //testFunGen(sai.llvm.LLSCExpr.externalFun, "externalFun", "@externalFun")
    //runLLSC(sai.llvm.LLSCExpr.complexStruct, "complexStruct", "@main")
    //runLLSC(sai.llvm.OOPSLA20Benchmarks.mp65536, "mp65536", "@f")
  }

  // @virtualize
  // def specializeFun(m: Module, name: String, fname: String): LLSCDriver[Int, Unit] =
  //   new LLSCDriver[Int, Unit](name, "./llsc_gen") {
  //     def snippet(u: Rep[Int]) = {
  //       val res = execExternal(m, fname)
  //       println(res)
  //     }
  // }

  // def testFunGen(m: Module, name: String, fname: String) {
  //   val res = sai.utils.Utils.time {
  //     val code = specializeFun(m, name + "gen", fname)
  //     code.genAll
  //   }
  //   println(res._2)
  // }
}
