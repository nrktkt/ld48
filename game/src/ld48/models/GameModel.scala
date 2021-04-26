package ld48.models

import indigo.shared.time.Seconds
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.RGBA
import ld48._
import ld48.controllers.Combat
import indigo.shared.scenegraph.Graphic
import indigo.shared.materials.Material
import indigo.shared.datatypes.Point

case class GameModel(
    time: Seconds,
    spawnTimer: Seconds,
    player1: Player,
    player2: Player,
    winner: Player,
    platforms: Seq[Platform],
    gameOver: Boolean,
    halted: Boolean = false
) {

  def update(t: Seconds): GameModel = {
    val speed = time.toFloat
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

    val updatedPlats =
      newPlats.map(_.update(speed, t, halted)).filter(_.y > -20)

    val updatedPlayer1 = player1.update(updatedPlats, t)
    val updatedPlayer2 = player2.update(updatedPlats, t)

    val (postCombatPlayer1, postCombatPlayer2) =
      Combat.update(updatedPlayer1, updatedPlayer2)

    val (updatedGameOver, updatedWinner) =
      if (postCombatPlayer1.position.y < -10) { (true, postCombatPlayer2) }
      else if (postCombatPlayer2.position.y < -10) { (true, postCombatPlayer1) }
      else { (false, postCombatPlayer1) }

    this.copy(
      player1 = postCombatPlayer1,
      player2 = postCombatPlayer2,
      platforms = updatedPlats,
      time = time + t,
      spawnTimer = updatedSpawn + t,
      gameOver = updatedGameOver,
      winner = updatedWinner
    )
  }

  def render =
    (player1.render ++ player2.render ++ platforms
      .flatMap(
        _.render
      )).toList

  def reset(): GameModel = GameModel.initial

  def renderBackground =
    for (i <- 0 until 165) yield {
      GameModel.background.moveTo(
        Point(
          (i % 11) * 32,
          ((i / 11) * 32)
        ) - Point(0, (time).toInt)
      )
    }
}

object GameModel {

  val background = Graphic(
    32,
    32,
    Material.Bitmap(HelloIndigo.backgroundAssetName)
  )

  def initial: GameModel =
    GameModel(
      time = Seconds(0),
      spawnTimer = Seconds(99), // always spawn one right away
      player1 = Player(
        "right",
        Vector2(100, 20),
        Vector2(0, 0),
        Vector2(0, 0),
        RGBA.Green,
        isPlayerOne = true
      ),
      player2 = Player(
        "left",
        Vector2(300, 20),
        Vector2(0, 0),
        Vector2(0, 0),
        RGBA.Red,
        isPlayerOne = false
      ),
      winner = Player(
        "left",
        Vector2(300, 300),
        Vector2(0, 0),
        Vector2(0, 0),
        RGBA.White
      ),
      platforms = Platform.initial,
      gameOver = false
    )

}
