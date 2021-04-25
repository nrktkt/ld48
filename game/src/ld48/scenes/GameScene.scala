package ld48.scenes

import ld48.models._
import indigo._
import indigo.scenes._
import ld48.HelloIndigo

object GameScene extends Scene[Unit, GlobalModel, Unit] {
  type SceneModel     = GameModel
  type SceneViewModel = Unit
  val rand = new scala.util.Random()

  val name = SceneName("game")
  def modelLens = Lens[GlobalModel, GameModel](
    _.game,
    (global, scene) => global.copy(game = scene)
  )
  def viewModelLens = Lens.identity
  def eventFilters  = EventFilters.Restricted.withViewModelFilter(_ => None)
  def subSystems    = Set.empty

  def normalizedRandom() = rand.between(-1.0, 1.0)

  def updateModel(context: FrameContext[Unit], model: SceneModel) = {
    //case Click(x, y) =>
    /*
      Outcome(
        model.addDot(
          Dot(
            Vector2(x, y),
            Vector2(normalizedRandom(), normalizedRandom()),
            100
          )
        )
      )
     */
    case KeyboardEvent.KeyDown(Key.LEFT_ARROW) =>
      Outcome(model.copy(leftDown = true))
    case KeyboardEvent.KeyDown(Key.RIGHT_ARROW) =>
      Outcome(model.copy(rightDown = true))
    case KeyboardEvent.KeyUp(Key.LEFT_ARROW) =>
      Outcome(model.copy(leftDown = false))
    case KeyboardEvent.KeyUp(Key.RIGHT_ARROW) =>
      Outcome(model.copy(rightDown = false))
    case KeyboardEvent.KeyDown(Key.KEY_Q) =>
      Outcome(model.copy(player = model.player.kick))
    case FrameTick =>
      Outcome(
        model.update(
          context.delta
        )
      )

    case _: GlobalEvent => Outcome(model)
  }

  def updateViewModel(
      context: FrameContext[Unit],
      model: SceneModel,
      viewModel: Unit
  ) = _ => Outcome(viewModel)

  def present(
      context: FrameContext[Unit],
      model: SceneModel,
      viewModel: Unit
  ) =
    Outcome(
      SceneUpdateFragment(
        Layer(
          HelloIndigo.gameBindingKey,
          //Graphic(Rectangle(0, 0, 32, 32), 1, Material.Bitmap(assetName)) ::
          model.render
        )
      )
    )
}
