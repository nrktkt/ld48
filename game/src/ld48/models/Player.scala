package ld48.models

import indigo._
import indigo.shared.datatypes.Vector2
import indigo.shared.time.Seconds
import ld48._
import indigo.shared.datatypes.Rectangle
import indigo.shared.scenegraph.Graphic
import indigo.shared.materials.Material
import Player._
import indigoextras.geometry.Polygon

case class Player(
    position: Vector2,
    velocity: Vector2,
    acceleration: Vector2,
    punchCooldown: Seconds = Seconds.zero,
    kickCooldown: Seconds = Seconds.zero
) {
  private val maxPunchCooldown = Seconds(.25)
  private val maxKickCooldown  = Seconds(.5)
  private val moveAcceleration = 20.0  // TODO Tune
  private val dragCoef         = 0.1
  private val maxSpeed         = 200.0 // super high drag?
  private val moveSpeed        = 20.0
  private val gravity          = Vector2(0, 5)

  val attackCoolingDown =
    punchCooldown > Seconds.zero || kickCooldown > Seconds.zero

  def kick =
    if (!attackCoolingDown)
      copy(kickCooldown = maxKickCooldown, acceleration = Vector2.zero)
    else this

  def update(
      platforms: Seq[Platform],
      leftDown: Boolean,
      rightDown: Boolean,
      t: Seconds
  ): Player = {
    val updatedAcceleration =
      if (attackCoolingDown)
        Vector2.zero
      else if (leftDown && !rightDown)
        Vector2(-1 * moveAcceleration, 0)
      else if (rightDown && !leftDown)
        Vector2(1 * moveAcceleration, 0)
      else
        Vector2.zero

    val effectiveDrag = if (updatedAcceleration == Vector2.zero) dragCoef else 1

    val newVelocity =
      (velocity + updatedAcceleration * t * moveSpeed) * effectiveDrag

    val precollisionPosition = {
      val Vector2(x, y) =
        position + (newVelocity * t) + updatedAcceleration * t * t * .5 + gravity
      val wallCorrectedX =
        math.min(
          HelloIndigo.viewportWidth / HelloIndigo.magnification - 16,
          math.max(0, x)
        )
      println(wallCorrectedX)
      Vector2(wallCorrectedX, y)
    }

    val collisionCorrectedPosition = platforms
      .map(platform => {
        platform.blockList.zipWithIndex
          .collect { case (Some(_), i) => i }
          .collectFirst(
            (
                i =>
                  playerCollidesBlock(
                    computeHitbox(precollisionPosition),
                    i,
                    platform.y
                  )
            ).unlift
          )
      })
      .collectFirst { case Some(y) => precollisionPosition.copy(y = y) }
      .getOrElse(precollisionPosition)

    this.copy(
      position = collisionCorrectedPosition,
      velocity = newVelocity,
      acceleration = Vector2(0, 0),
      punchCooldown = punchCooldown - t,
      kickCooldown = kickCooldown - t
    )
  }

  val hitbox = Player.computeHitbox(position)

  def render = Seq(
    Graphic(32, 32, Material.Bitmap(HelloIndigo.playerAssetName))
      //.withRef(16, 32)
      //.withScale(Vector2(.8, .8))
      .moveTo(position.toPoint),
    Shape.Box(hitbox, Fill.Color(RGBA.Red), Stroke(5, RGBA.Red))
    //.moveTo(position.toPoint)
  )

}

object Player {
  def computeHitbox(position: Vector2) = {
    val ul = position + Vector2(10, 26)
    val lr = ul + Vector2(12, 6)
    Rectangle.fromTwoPoints(ul.toPoint, lr.toPoint)
  }

  // return corrected player y value
  def playerCollidesBlock(
      playerHitbox: Rectangle,
      blockI: Int,
      blockY: Double
  ): Option[Double] = {
    val blockHitbox = Block.hitbox(blockY, blockI)
    val blockUL     = blockHitbox.topLeft
    val blockLR     = blockHitbox.bottomRight

    val playerUL = playerHitbox.topLeft
    val playerLR = playerHitbox.bottomRight

    if (
      playerUL.x < blockLR.x &&
      playerLR.x > blockUL.x &&
      playerUL.y < blockLR.y &&
      playerLR.y > blockUL.y
    ) {
      Some(blockY - 32)
    } else None
  }
}
