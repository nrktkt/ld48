package ld48.models

case class GlobalModel(game: GameModel)
object GlobalModel {
  def initial = GlobalModel(
    GameModel.initial
  )
}
