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

case class Player(
    facing: Either["left", "right"],
    position: Vector2,
    velocity: Vector2,
    acceleration: Vector2,
    leftDown: Boolean = false,
    rightDown: Boolean = false,
    punchCooldown: Seconds = Seconds.zero,
    kickCooldown: Seconds = Seconds.zero,
    stunCooldown: Seconds = Seconds.zero
) {
  private val maxStunCooldown  = Seconds(1)
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

  def punching = punchCooldown > Seconds.zero
  def kicking  = kickCooldown > Seconds.zero

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

  val hitbox = Player.computeHitbox(position, true)

  val attackHitbox =
    if (punchCooldown > Seconds.zero)
      if (facing.isRight)
        Rectangle(position.moveBy(Vector2(16, 16)).toPoint, Point(17, -5))
      else
        Rectangle(position.moveBy(Vector2(16, 16)).toPoint, Point(-17, -5))
    else if (kickCooldown > Seconds.zero)
      if (facing.isRight) Rectangle(hitbox.bottomLeft, Point(27, -5))
      else Rectangle(hitbox.bottomRight, Point(-27, -5))
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

  def render = Seq(
    Graphic(
      32,
      32,
      Material.ImageEffects(HelloIndigo.playerAssetName).withTint(RGBA.Red)
    ) //Material.Bitmap(HelloIndigo.playerAssetName))
      //.withMaterial(Material.ImageEffects)
      .flipHorizontal(facing.isLeft)
      //.withRef(16, 32)
      //.withScale(Vector2(.8, .8))
      .moveTo(position.toPoint)

    //.withMaterial(

    //)
    /*
      .modifyMaterial {
        case m: LegacyEffects => m.withTint(RGBA.Red)
        case m                => m
      }*/,
    Shape
      .Polygon(
        Fill.Color(RGBA.Zero),
        Stroke(3, RGBA.Red)
      )(
        hitbox.topLeft,
        hitbox.topRight,
        hitbox.bottomRight,
        hitbox.bottomLeft
      ),
    Shape
      .Polygon(
        Fill.Color(RGBA.Zero),
        Stroke(2, RGBA.Red)
      )(
        attackHitbox.topLeft,
        attackHitbox.topRight,
        attackHitbox.bottomRight,
        attackHitbox.bottomLeft
      )
    //.Box(hitbox, Fill.Color(RGBA.Zero), Stroke(3, RGBA.Red))
    //Graphic(attackHitbox, 2, Material.Bitmap(HelloIndigo.hitboxAssetName))

    //Shape.Box(attackHitbox, Fill.Color(RGBA.Zero), Stroke(2, RGBA.Red))
    //.moveTo(position.toPoint)
  )
}

object Player {
  def computeHitbox(position: Vector2, isAttack: Boolean) = {
    if (isAttack) {
      val ul = position + Vector2(6, 0)
      val lr = ul + Vector2(20, 32)
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
