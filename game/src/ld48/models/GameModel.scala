package ld48.models

import indigo.shared.time.Seconds
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.RGBA
import ld48._

case class GameModel(
    time: Seconds,
    spawnTimer: Seconds,
    player1: Player,
    player2: Player,
    platforms: Seq[Platform],
    gameOver: Boolean
) {

  def update(t: Seconds): GameModel = {
    var speed = time.toFloat
    val (newPlats, updatedSpawn) = {
      if (spawnTimer.toDouble > (2.0 - (time.toFloat / 60.0)))
        (
          platforms ++ Seq(
            Platform.addPlatform(900.0)
          ),
          Seconds(0)
        )
      else
        (platforms, spawnTimer)
    }

    val updatedPlats = newPlats.map(_.update(speed, t)).filter(_.y > -20)

    var updatedPlayer1 = player1.update(updatedPlats, t)
    var updatedPlayer2 = player2.update(updatedPlats, t)

    var updatedGameOver =
      if (updatedPlayer1.position.y < -10 || updatedPlayer2.position.y < -10)
        true
      else false

    this.copy(
      player1 = updatedPlayer1,
      player2 = updatedPlayer2,
      platforms = updatedPlats,
      time = time + t,
      spawnTimer = updatedSpawn + t,
      gameOver = updatedGameOver
    )
  }

  def render =
    (player1.render ++ player2.render ++ platforms.flatMap(_.render)).toList

  def reset() : GameModel = GameModel.initial

}

object GameModel {

  def initial: GameModel =
    GameModel(
      time = Seconds(0),
      spawnTimer = Seconds(99), // always spawn one right away
      player1 = Player("right", Vector2(100, 20), Vector2(0, 0), Vector2(0, 0), RGBA.Green),
      player2 = Player("left", Vector2(300, 20), Vector2(0, 0), Vector2(0, 0), RGBA.Red),
      platforms = Platform.initial,
      gameOver = false
    )

}
