package ld48.scenes

import ld48.models._
import indigo._
import indigo.scenes._
import ld48.HelloIndigo

object EndGameScene extends Scene[Unit, GlobalModel, Unit] {
  type SceneModel     = GameModel
  type SceneViewModel = Unit

  val name = SceneName("gameover")
  def modelLens = Lens[GlobalModel, GameModel](
    _.game,
    (global, scene) => global.copy(game = scene)
  )
  def viewModelLens = Lens.identity
  def eventFilters  = EventFilters.Restricted.withViewModelFilter(_ => None)
  def subSystems    = Set.empty

  def updateModel(context: FrameContext[Unit], model: SceneModel) = {
    case KeyboardEvent.KeyUp(Key.SPACE) =>
      Outcome(model.reset()).addGlobalEvents(SceneEvent.JumpTo(GameScene.name))

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
          HelloIndigo.gameBindingKey
        )
      )
    )
}
