package ld48

import indigo._
import scala.scalajs.js.annotation.JSExportTopLevel
import indigo.scenes.Scene
import indigo.scenes.SceneName
import indigo.scenes.Lens
import java.util.Random
import indigo.shared.events
import events.MouseEvent.Click
import scala.concurrent.duration._
import scala.language.postfixOps
import ld48._
import HelloIndigo.magnification
import indigo.shared.materials
import indigoextras.effectmaterials.Border
import ld48.models._
import ld48.scenes._
import ld48.Player1
import ld48.Player2

@JSExportTopLevel("IndigoGame")
object HelloIndigo extends IndigoGame[Unit, Unit, GlobalModel, Unit] {

  val gameBindingKey = BindingKey("game")

  def scenes(bootData: Unit)       = NonEmptyList(GameScene, EndGameScene)
  def initialScene(bootData: Unit) = Some(GameScene.name)
  val eventFilters                 = EventFilters.Restricted
  def boot(flags: Map[String, String]) = Outcome(
    BootResult.configOnly(config).withAssets(assets)
  )
  def initialViewModel(startupData: Unit, model: GlobalModel) = Outcome(())

  def updateViewModel(
      context: FrameContext[Unit],
      model: GlobalModel,
      viewModel: Unit
  ) = _ => Outcome(())

  def present(
      context: FrameContext[Unit],
      model: GlobalModel,
      viewModel: Unit
  ) =
    Outcome(SceneUpdateFragment.empty.addLayer(Layer(gameBindingKey)))

  val magnification = 2

  val viewportWidth = 704

  val config =
    GameConfig.default
      .withViewport(viewportWidth, 900)
      .withMagnification(magnification)
      .useWebGL2

  val animations: Set[Animation] =
    Set.empty

  val assetName           = AssetName("dots")
  val blockAssetName      = AssetName("block")
  val hitboxAssetName     = AssetName("hitbox")
  val backgroundAssetName = AssetName("background")

  val assets: Set[AssetType] =
    Set(
      AssetType
        .Image(backgroundAssetName, AssetPath("assets/background-tile.png")),
      AssetType.Image(blockAssetName, AssetPath("assets/steel-beams.png")),
      AssetType.Image(hitboxAssetName, AssetPath("assets/red-box.png"))
    ) ++ Player1.assets("") ++ Player2.assets("")

  val fonts: Set[FontInfo] = Set()

  val shaders: Set[Shader] = Set()

  def setup(
      viewModel: Unit,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[GlobalModel] =
    Outcome(
      GlobalModel.initial
    )

  def updateModel(
      context: FrameContext[Unit],
      model: GlobalModel
  ): GlobalEvent => Outcome[GlobalModel] = { case _ =>
    Outcome(model)
  }
}
