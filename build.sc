import mill._, scalalib._
import mill.scalajslib._
import mill.scalajslib.api._

import $ivy.`io.indigoengine::mill-indigo:0.7.0`, millindigo._

object game extends ScalaJSModule with MillIndigo {
  def scalaVersion   = "2.13.5"
  def scalaJSVersion = "1.5.1"

  val gameAssetsDirectory = os.pwd / "assets"
  val showCursor          = true
  val title               = "LD 48 Game"
  val windowStartWidth    = 1280
  val windowStartHeight   = 720

  def ivyDeps = Agg(
    ivy"io.indigoengine::indigo-json-circe::0.7.0",
    ivy"io.indigoengine::indigo::0.7.1"
  )

  def buildGame() = T.command {
    T {
      compile()
      fastOpt()
      indigoBuild()()
    }
  }

  def runGame() = T.command {
    T {
      compile()
      fastOpt()
      indigoRun()()
    }
  }

  def buildGameFull() = T.command {
    T {
      compile()
      fullOpt()
      indigoBuildFull()()
    }
  }

  def runGameFull() = T.command {
    T {
      compile()
      fullOpt()
      indigoRunFull()()
    }
  }
}
