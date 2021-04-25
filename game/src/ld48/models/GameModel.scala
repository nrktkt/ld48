package ld48.models

import indigo.shared.time.Seconds
import indigo.shared.datatypes.Vector2

case class GameModel(
    leftDown: Boolean,
    rightDown: Boolean,
    time: Seconds,
    spawnTimer: Seconds,
    player: Player,
    platforms: Seq[Platform]
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

    val updatedPlayer = player.update(updatedPlats, leftDown, rightDown, t)

    // return corrected player y value
    def playerCollidesBlock(
        player: Player,
        blockI: Int,
        blockY: Double
    ): Option[Double] = {
      val blockHitbox = Block.hitbox(blockY, blockI)
      val blockUL     = blockHitbox.topLeft
      val blockLR     = blockHitbox.bottomRight

      val playerUL = player.hitbox.topLeft
      val playerLR = player.hitbox.bottomRight
      if (
        playerUL.x < blockLR.x &&
        playerLR.x > blockUL.x &&
        playerUL.y < blockLR.y &&
        playerLR.y > blockUL.y
      ) {
        Some(blockY - 32)
      } else None
    }

    val maybeCollisionBlock = updatedPlats
      .map(platform => {
        platform.blockList.zipWithIndex
          .collect { case (Some(_), i) => i }
          .collectFirst(
            (
                i => playerCollidesBlock(updatedPlayer, i, platform.y)
            ).unlift
          )
      })
      .collectFirst { case Some(d) => d }

    val collisionCorrectedPlayer = maybeCollisionBlock match {
      case None => updatedPlayer
      case Some(y) =>
        updatedPlayer.copy(position = updatedPlayer.position.copy(y = y))
    }

    this.copy(
      player = collisionCorrectedPlayer,
      platforms = updatedPlats,
      time = time + t,
      spawnTimer = updatedSpawn + t
    )
  }

  def render = (player.render ++ platforms.flatMap(_.render)).toList

}

object GameModel {

  def initial: GameModel =
    GameModel(
      leftDown = false,
      rightDown = false,
      time = Seconds(0),
      spawnTimer = Seconds(99), // always spawn one right away
      player = Player(Vector2(300, 20), Vector2(0, 0), Vector2(0, 0)),
      platforms = Platform.initial
    )

}
