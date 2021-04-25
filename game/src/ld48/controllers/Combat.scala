package ld48.controllers

import ld48.models.Player
import indigo.shared.time.Seconds

object Combat {
  def update(player1: Player, player2: Player): (Player, Player) =
    if (player1.attackHitbox.overlaps(player2.hitbox))
      if (player1.punching)
        (player1, player2.knockUp)
      else if (player1.kicking)
        (player1, player2.stun)
      else ???
    else if (player2.attackHitbox.overlaps(player1.hitbox))
      if (player2.punching)
        (player1.knockUp, player2)
      else if (player2.kicking)
        (player1.stun, player2)
      else ???
    else (player1, player2)

}
