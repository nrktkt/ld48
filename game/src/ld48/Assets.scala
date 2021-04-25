package ld48

import indigo._
import indigo.json.Json

object Player1 {
  val ref: AssetName     = AssetName("down-dude-sheet-p1")
  val jsonRef: AssetName = AssetName("player2-json")

  def assets(baseUrl: String): Set[AssetType] =
    Set(
      AssetType.Image(
        Player1.ref,
        AssetPath(baseUrl + "assets/" + Player1.ref.value + ".png")
      ),
      AssetType.Text(
        Player1.jsonRef,
        AssetPath(baseUrl + "assets/" + Player1.ref.value + ".json")
      )
    )
}

object Player2 {
  val ref: AssetName     = AssetName("down-dude-sheet-p2")
  val jsonRef: AssetName = AssetName("player2-json")

  def assets(baseUrl: String): Set[AssetType] =
    Set(
      AssetType.Image(
        Player2.ref,
        AssetPath(baseUrl + "assets/" + Player2.ref.value + ".png")
      ),
      AssetType.Text(
        Player2.jsonRef,
        AssetPath(baseUrl + "assets/" + Player2.ref.value + ".json")
      )
    )
}

object Loader {

  def loadAnimation(
      assetCollection: AssetCollection,
      dice: Dice
  )(
      jsonRef: AssetName,
      name: AssetName,
      depth: Depth
  ): SpriteAndAnimations = {
    val res = for {
      json                <- assetCollection.findTextDataByName(jsonRef)
      aseprite            <- Json.asepriteFromJson(json)
      spriteAndAnimations <- aseprite.toSpriteAndAnimations(dice, name)
    } yield spriteAndAnimations.copy(sprite =
      spriteAndAnimations.sprite.withDepth(depth)
    )

    res.get
  }
}
