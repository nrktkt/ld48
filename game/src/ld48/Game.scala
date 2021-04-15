import indigo._
import scala.scalajs.js.annotation.JSExportTopLevel
import indigo.scenes.Scene
import indigo.scenes.SceneName
import indigo.scenes.Lens
import java.util.Random
import indigo.shared.events
import events.MouseEvent.Click

@JSExportTopLevel("IndigoGame")
object HelloIndigo extends IndigoGame[Unit, Unit, Model, Unit] {

  val gameBindingKey = BindingKey("game")

  def scenes(bootData: Unit)       = NonEmptyList(GameScene)
  def initialScene(bootData: Unit) = Some(GameScene.name)
  val eventFilters                 = EventFilters.Restricted
  def boot(flags: Map[String, String]) = Outcome(
    BootResult.configOnly(config).withAssets(assets)
  )
  def initialViewModel(startupData: Unit, model: Model) = Outcome(())

  def updateViewModel(
      context: FrameContext[Unit],
      model: Model,
      viewModel: Unit
  ) = _ => Outcome(())

  def present(context: FrameContext[Unit], model: Model, viewModel: Unit) =
    Outcome(SceneUpdateFragment.empty.addLayer(Layer(gameBindingKey)))

  val magnification = 1

  val config: GameConfig =
    GameConfig.default.withViewport(1280, 720).withMagnification(magnification)

  val animations: Set[Animation] =
    Set.empty

  val assetName = AssetName("dots")

  val assets: Set[AssetType] =
    Set(
      AssetType.Image(assetName, AssetPath("assets/dots.png"))
    )

  val fonts: Set[FontInfo] = Set()

  val shaders: Set[Shader] = Set()

  def setup(
      viewModel: Unit,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[Model] =
    Outcome(
      Model.initial(
        //config.viewport.giveDimensions(magnification).center
      )
    )

  def updateModel(
      context: FrameContext[Unit],
      model: Model
  ): GlobalEvent => Outcome[Model] = { case _ =>
    Outcome(model)
  }
}

case class Model(dots: List[Dot]) {
  def addDot(dot: Dot): Model =
    this.copy(dots = dot :: dots)

  def update(screenBounds: (Vector2, Vector2), timeDelta: Seconds): Model =
    this.copy(dots = dots.map(_.update(screenBounds, timeDelta)))
}

object Model {
  def initial(): Model = Model(
    List(
      //Dot(Vector2(1, 1), Vector2(1, 1), 100),
      //Dot(Vector2(10, 5), Vector2(-1, .5), 100)
    )
  )
}

case class Dot(position: Vector2, direction: Vector2, health: Int) {
  def update(screenBounds: (Vector2, Vector2), timeDelta: Seconds) = {
    val normalizedDirection     = direction.normalise
    val nextPosition            = position + direction * timeDelta.value * 1000
    val (upperLeft, lowerRight) = screenBounds
    val wrapped = Vector2(
      if (nextPosition.x > lowerRight.x) upperLeft.x
      else if (nextPosition.x < upperLeft.x) lowerRight.x
      else nextPosition.x,
      if (nextPosition.y > lowerRight.y) upperLeft.y
      else if (nextPosition.y < upperLeft.y) lowerRight.y
      else nextPosition.y
    )

    this.copy(position = wrapped, direction = normalizedDirection)
  }
}

object GameScene extends Scene[Unit, Model, Unit] {
  type SceneModel     = Model
  type SceneViewModel = Unit
  val rand = new scala.util.Random()

  val name          = SceneName("game")
  def modelLens     = Lens.keepLatest
  def viewModelLens = Lens.identity
  def eventFilters  = EventFilters.Restricted.withViewModelFilter(_ => None)
  def subSystems    = Set.empty

  def normalizedRandom() = rand.between(-1.0, 1.0)

  def updateModel(context: FrameContext[Unit], model: SceneModel) = {
    case Click(x, y) =>
      Outcome(
        model.addDot(
          Dot(
            Vector2(x, y),
            Vector2(normalizedRandom(), normalizedRandom()),
            100
          )
        )
      )

    case FrameTick =>
      Outcome(
        model.update(
          (Vector2(-8, -8), Vector2(1288, 728)),
          context.delta
        )
      )

    case _ => Outcome(model)
  }

  def updateViewModel(
      context: FrameContext[Unit],
      model: SceneModel,
      viewModel: Unit
  ) = _ => Outcome(viewModel)

  def present(
      context: FrameContext[Unit],
      model: SceneModel,
      viewModel: Unit
  ) =
    Outcome(
      SceneUpdateFragment(
        Layer(
          HelloIndigo.gameBindingKey,
          //Graphic(Rectangle(0, 0, 32, 32), 1, Material.Bitmap(assetName)) ::
          drawDots(model.dots)
        )
      )
    )

  def drawDots(
      dots: List[Dot]
  ): List[Graphic] =
    dots.map { dot =>
      val position = dot.position.toPoint

      Graphic(
        Rectangle(0, 0, 32, 32),
        1,
        Material.Bitmap(HelloIndigo.assetName)
      )
        .withCrop(Rectangle(16, 16, 16, 16))
        .withRef(8, 8)
        .moveTo(position)
    }
}
