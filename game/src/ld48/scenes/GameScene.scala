package ld48.scenes

import ld48.models._
import indigo._
import indigo.scenes._
import ld48._

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

    case KeyboardEvent.KeyDown(Key.KEY_J) =>
      Outcome(model.copy(player2 = model.player2.move("left", true)))
    case KeyboardEvent.KeyDown(Key.KEY_L) =>
      Outcome(model.copy(player2 = model.player2.move("right", true)))
    case KeyboardEvent.KeyUp(Key.KEY_J) =>
      Outcome(model.copy(player2 = model.player2.move("left", false)))
    case KeyboardEvent.KeyUp(Key.KEY_L) =>
      Outcome(model.copy(player2 = model.player2.move("right", false)))
    case KeyboardEvent.KeyDown(Key.KEY_U) =>
      Outcome(model.copy(player2 = model.player2.kick))
    case KeyboardEvent.KeyDown(Key.KEY_O) =>
      Outcome(model.copy(player2 = model.player2.punch))

    case KeyboardEvent.KeyDown(Key.KEY_A) =>
      Outcome(model.copy(player1 = model.player1.move("left", true)))
    case KeyboardEvent.KeyDown(Key.KEY_D) =>
      Outcome(model.copy(player1 = model.player1.move("right", true)))
    case KeyboardEvent.KeyUp(Key.KEY_A) =>
      Outcome(model.copy(player1 = model.player1.move("left", false)))
    case KeyboardEvent.KeyUp(Key.KEY_D) =>
      Outcome(model.copy(player1 = model.player1.move("right", false)))
    case KeyboardEvent.KeyDown(Key.KEY_Q) =>
      Outcome(model.copy(player1 = model.player1.kick))
    case KeyboardEvent.KeyDown(Key.KEY_E) =>
      Outcome(model.copy(player1 = model.player1.punch))

    case KeyboardEvent.KeyDown(Key.KEY_0) =>
      Outcome(model.copy(halted = !model.halted))

    case FrameTick if (!model.gameOver) =>
      Outcome(
        model.update(
          context.delta
        )
      )

    case FrameTick if (model.gameOver) =>
      Outcome(
        model
      ).addGlobalEvents(SceneEvent.JumpTo(EndGameScene.name))

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
          model.render
        )
      )
    )
}
