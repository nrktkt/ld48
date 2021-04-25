package ld48.controllers

import ld48.models.Player
import indigo.shared.time.Seconds

object Combat {
  def update(player1: Player, player2: Player): (Player, Player) =
    if (player1.attackHitbox.overlaps(player2.hitbox))
      if (player1.punching) ???
      else if (player1.kicking) {
        println("player 2 stunned")
        (player1, player2.stun)
      } else ???
    else if (player2.attackHitbox.overlaps(player1.hitbox))
      if (player2.punching) ???
      else if (player2.kicking) {
        println("player 1 stunned")
        (player1.stun, player2)
      } else ???
    else (player1, player2)

}
