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
import indigoextras.effectmaterials.LegacyEffects
import ld48.Loader
import ld48.Player1
import ld48.Player2

case class Player(
    facing: Either["left", "right"],
    position: Vector2,
    velocity: Vector2,
    acceleration: Vector2,
    color: RGBA = RGBA.White,
    leftDown: Boolean = false,
    rightDown: Boolean = false,
    punchCooldown: Seconds = Seconds.zero,
    kickCooldown: Seconds = Seconds.zero,
    stunCooldown: Seconds = Seconds.zero,
    isPlayerOne: Boolean = true
) {
  private val maxStunCooldown  = Seconds(2)
  private val maxPunchCooldown = Seconds(.25)
  private val maxKickCooldown  = Seconds(.5)
  private val moveAcceleration = 50.0  // TODO Tune
  private val dragCoef         = 0.1
  private val maxSpeed         = 200.0 // super high drag?
  private val moveSpeed        = 20.0
  private val gravity          = Vector2(0, 5)

  val attackCoolingDown =
    stunCooldown > Seconds.zero || punchCooldown > Seconds.zero || kickCooldown > Seconds.zero

  def move(direction: Either["left", "right"], down: Boolean) =
    direction match {
      case Left(_)  => copy(leftDown = down)
      case Right(_) => copy(rightDown = down)
    }

  def punching = punchCooldown / 2 > Seconds.zero
  def kicking  = kickCooldown / 2 > Seconds.zero
  def stunned  = stunCooldown > Seconds.zero

  def kick =
    if (!attackCoolingDown)
      copy(kickCooldown = maxKickCooldown, acceleration = Vector2.zero)
    else this

  def punch =
    if (!attackCoolingDown)
      copy(punchCooldown = maxPunchCooldown, acceleration = Vector2.zero)
    else this

  def stun =
    if (stunCooldown <= Seconds.zero) // if not already stunned
      copy(stunCooldown = maxStunCooldown)
    else this

  def knockUp = copy(velocity = velocity + Vector2(0, -25000))

  val hitbox = Player.computeHitbox(position, true)

  val attackHitbox =
    if (punching)
      if (facing.isRight)
        Rectangle(
          position.moveBy(14, 15).toPoint,
          punchSize
        )
      else
        Rectangle(
          position.moveBy(18, 15).toPoint - punchSize.withY(0),
          punchSize //                      ^
        )           // rectangle collision is broken for negative sizes, so origin can't be top right
    else if (kicking)
      if (facing.isRight)
        Rectangle(position.moveBy(16, 22).toPoint, kickSize)
      else
        Rectangle(
          position.moveBy(16, 22).toPoint - kickSize.withY(0),
          kickSize //                       ^
        )          //               same as for punch
    else
      Rectangle.zero

  def update(
      platforms: Seq[Platform],
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

    val newVelocity = {
      val Vector2(x, y) =
        (velocity + updatedAcceleration * t * moveSpeed) * effectiveDrag
      Vector2(math.max(math.min(maxSpeed, x), maxSpeed * -1), y)
    }

    val precollisionPosition = {
      val Vector2(x, y) =
        position + (newVelocity * t) + updatedAcceleration * t * t * .5 + gravity
      val wallCorrectedX =
        math.min(
          HelloIndigo.viewportWidth / HelloIndigo.magnification - 16,
          math.max(0, x)
        )
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
                    computeHitbox(precollisionPosition, false),
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
      stunCooldown = stunCooldown - t,
      punchCooldown = punchCooldown - t,
      kickCooldown = kickCooldown - t,
      facing = velocity.x match {
        case x if x > 0.01  => "right"
        case x if x < -0.01 => "left"
        case _              => facing
      }
    )
  }

  def renderPlayer =
    if (stunned) renderStunned(isPlayerOne)
    else if (kicking) renderKick(isPlayerOne)
    else if (punching) renderPunch(isPlayerOne)
    else renderNormal(isPlayerOne)

  def render = Seq(
    renderPlayer
      .flipHorizontal(facing.isLeft)
      .moveTo(position.toPoint),
    Shape.Polygon(hitbox.corners, Fill.None, Stroke(3, RGBA.Green)),
    Shape.Polygon(attackHitbox.corners, Fill.None, Stroke(2, RGBA.Green))
  )
}

object Player {
  val kickSize  = Point(13, 6)
  val punchSize = Point(16, 5)

  def renderSheet(isPlayerOne: Boolean) = Graphic(
    32,
    32,
    if (isPlayerOne) Material.Bitmap(Player1.ref)
    else Material.Bitmap(Player2.ref)
  )

  def renderNormal(isPlayerOne: Boolean) =
    renderSheet(isPlayerOne).withCrop(0, 0, 32, 32)
  def renderStunned(isPlayerOne: Boolean) =
    renderSheet(isPlayerOne).withCrop(1 * 32, 0, 32, 32)
  def renderPunch(isPlayerOne: Boolean) =
    renderSheet(isPlayerOne).withCrop(2 * 32, 0, 32, 32)
  def renderKick(isPlayerOne: Boolean) =
    renderSheet(isPlayerOne).withCrop(3 * 32, 0, 32, 32)

  def computeHitbox(position: Vector2, isAttack: Boolean) = {
    if (isAttack) {
      val ul = position + Vector2(7, 1)
      val lr = ul + Vector2(16, 31)
      Rectangle.fromTwoPoints(ul.toPoint, lr.toPoint)
    } else {
      val ul = position + Vector2(10, 26)
      val lr = ul + Vector2(12, 6)
      Rectangle.fromTwoPoints(ul.toPoint, lr.toPoint)
    }
  }

  // return corrected player y value
  def playerCollidesBlock(
      playerHitbox: Rectangle,
      blockI: Int,
      blockY: Double
  ): Option[Double] = {
    val blockHitbox = Block.hitbox(blockY, blockI)

    if (blockHitbox.overlaps(playerHitbox))
      Some(blockY - 32)
    else None
  }
}
