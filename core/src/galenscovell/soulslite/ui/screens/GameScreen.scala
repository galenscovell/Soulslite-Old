package galenscovell.soulslite.ui.screens

import aurelienribon.tweenengine.TweenManager
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx._
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.graphics._
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math._
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.scenes.scene2d.Stage
import galenscovell.soulslite.Main
import galenscovell.soulslite.actors.components._
import galenscovell.soulslite.environment.{Physics, TileMap}
import galenscovell.soulslite.processing._
import galenscovell.soulslite.processing.generation.{BehaviorCreator, EntityCreator}
import galenscovell.soulslite.processing.pathfinding.PathfindDebugRenderer
import galenscovell.soulslite.util.Constants


class GameScreen(root: Main) extends AbstractScreen(root) {
  private val entityBatch: SpriteBatch = new SpriteBatch()
  private val worldCamera: OrthographicCamera =
    new OrthographicCamera(Constants.SCREEN_X, Constants.SCREEN_Y)

  private val tweenManager: TweenManager = new TweenManager
  private val controllerHandler: ControllerHandler = new ControllerHandler
  private val physics: Physics = new Physics
  private val tileMap: TileMap = new TileMap(physics.getWorld, "test")
  private val entityManager: EntityManager =
    new EntityManager(entityBatch, controllerHandler, physics.getWorld, tileMap.getAStarGraph, this)
  private val pathfindDebugRenderer: PathfindDebugRenderer = new PathfindDebugRenderer(tileMap.getAStarGraph)

  // Box2d has a limit on velocity of 2.0 units per step
  // The max speed is 120m/s at 60fps
  private val timestep: Float = 1 / 120.0f
  private var accumulator: Float = 0

  // For shader
  private var environmentShader: ShaderProgram = _
  private var environmentShaderTime: Float = 0f
  private var steps: Int = 0
  private var totalRunTimes: Double = 0f

  // For camera
  private val lerpPos: Vector3 = new Vector3(0, 0, 0)
  private var minCamX, minCamY, maxCamX, maxCamY: Float = 0f
  private var playerBody: Body = _

  create()


  /********************
    *       Init      *
    ********************/
  override def create(): Unit = {
    stage = new Stage(viewport, root.spriteBatch)

    val entityCreator: EntityCreator = new EntityCreator(entityManager.getEngine, physics.getWorld)
    val behaviorCreator: BehaviorCreator = new BehaviorCreator(physics.getWorld)

    // Establish player entity
    val player: Entity = entityCreator.makePlayer("player", Constants.MID_ENTITY_SIZE, 20, 20, 5, 20)
    playerBody = player.getComponent(classOf[BodyComponent]).body
    val playerSteerable: BaseSteerable = player.getComponent(classOf[SteeringComponent]).getSteerable

    // Start camera immediately centered on player
    worldCamera.position.set(playerBody.getPosition.x, playerBody.getPosition.y, 0)


    // Temp entities for testing purposes with same graphics as player
//    for (x <- 0 until 1) {
//      val dummy = entityCreator.fromJson("testEntity", 18 + x, 18 + x, playerSteerable)
//      val dummySteerable: BaseSteerable = dummy.getComponent(classOf[SteeringComponent]).getSteerable

//      dummySteerable.behavior = behaviorCreator.makeBlendedSteering(
//        dummySteerable,
//        List[SteeringBehavior[Vector2]](
//          behaviorCreator.makeRaycastAvoidBehavior(dummySteerable),
//          behaviorCreator.makeCollisionAvoidBehavior(dummySteerable, tileMap.getPropSteerables),
//          behaviorCreator.makeWanderBehavior(dummySteerable)
//          // behaviorCreator.makePursueBehavior(dummySteerable, playerSteerable)
//          //behaviorCreator.makeHideBehavior(dummySteerable, playerSteerable, tileMap.getPropSteerables)
//          //behaviorCreator.makeArriveBehavior(dummySteerable, playerSteerable)
//        ),
//        List[Float](1f, 1f, 0.5f)
//      )

//      dummySteerable.behavior = behaviorCreator.makePrioritySteering(
//        dummySteerable,
//        List[SteeringBehavior[Vector2]](
//          behaviorCreator.makeRaycastAvoidBehavior(dummySteerable),
//          behaviorCreator.makeCollisionAvoidBehavior(dummySteerable, tileMap.getPropSteerables),
//          behaviorCreator.makeWanderBehavior(dummySteerable)
//          // behaviorCreator.makePursueBehavior(dummySteerable, playerSteerable)
//          //behaviorCreator.makeHideBehavior(dummySteerable, playerSteerable, tileMap.getPropSteerables)
//          //behaviorCreator.makeArriveBehavior(dummySteerable, playerSteerable)
//        )
//      )
//    }

    setupEnvironmentShader()
  }

  private def setupEnvironmentShader(): Unit = {
    ShaderProgram.pedantic = false

    environmentShader = new ShaderProgram(
      Gdx.files.internal("shaders/water_ripple.vert").readString(),
      Gdx.files.internal("shaders/water_ripple.frag").readString()
    )
    if (!environmentShader.isCompiled) {
      println(environmentShader.getLog)
    }

    tileMap.updateShader(environmentShader)
  }


/**********************
  *  Misc Operations  *
  **********************/
  def getRoot: Main = {
    root
  }

  def toMainMenu(): Unit = {
    root.setScreen(root.mainMenuScreen)
  }


  /**********************
    *      Camera       *
    **********************/
  private def updateCamera(): Unit = {
    // Find camera upper left coordinates
    minCamX = worldCamera.position.x - (worldCamera.viewportWidth / 2) * worldCamera.zoom
    minCamY = worldCamera.position.y - (worldCamera.viewportHeight / 2) * worldCamera.zoom
    // Find camera lower right coordinates
    maxCamX = minCamX + worldCamera.viewportWidth * worldCamera.zoom
    maxCamY = minCamY + worldCamera.viewportHeight * worldCamera.zoom
    worldCamera.update()
  }

  private def centerCameraOnPlayer(): Unit = {
    lerpPos.x = playerBody.getPosition.x
    lerpPos.y = playerBody.getPosition.y

    worldCamera.position.lerp(lerpPos, 0.05f)
  }

  def inCamera(x: Float, y: Float): Boolean = {
    // Determines if a point falls within the camera
    // (+/- medium entity size to reduce chance of pop-in)
    (x + Constants.MID_ENTITY_SIZE) >= minCamX &&
      (y + Constants.MID_ENTITY_SIZE) >= minCamY &&
      (x - Constants.MID_ENTITY_SIZE) <= maxCamX &&
      (y - Constants.MID_ENTITY_SIZE) <= maxCamY
  }


  /**********************
    * Screen Operations *
    **********************/
  override def render(delta: Float): Unit = {
    val startTime: Double = System.nanoTime()

    Gdx.gl.glClearColor(0, 0, 0, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    // Environment shader
    environmentShaderTime += delta
    if (environmentShaderTime > 1200) {
      environmentShaderTime = 0f
    }
    environmentShader.begin()
    environmentShader.setUniformf("u_time", environmentShaderTime)
    environmentShader.end()

    val frameTime: Float = Math.min(delta, 0.25f)
    accumulator += frameTime
    while (accumulator > timestep) {
      physics.update(timestep)
      accumulator -= timestep
    }

    // Camera operations
    updateCamera()
    centerCameraOnPlayer()
    entityBatch.setProjectionMatrix(worldCamera.combined)
    tileMap.updateCamera(worldCamera)

    // Main render operations
    tileMap.renderBaseLayer()
    entityBatch.begin()
    entityManager.update(delta)
    pathfindDebugRenderer.render(entityBatch)
    entityBatch.end()
    tileMap.renderOverlapLayer()

    // Update tweens
    tweenManager.update(delta)

    physics.debugRender(worldCamera.combined)

    // stage.act()
    // stage.draw()

    if (steps == 300) {
      println(f"Average: ${totalRunTimes / 300}%1.3f ms")
      steps = 0
      totalRunTimes = 0f
    }
    totalRunTimes += (System.nanoTime() - startTime) / 1000000
    steps += 1
  }

  override def show(): Unit = {
    Gdx.input.setInputProcessor(stage)
    Controllers.clearListeners()
    Controllers.addListener(controllerHandler)
  }

  override def resize(width: Int, height: Int): Unit = {
    super.resize(width, height)
    environmentShader.begin()
    environmentShader.setUniformf("u_resolution", width, height)
    environmentShader.end()
  }

  override def dispose(): Unit = {
    super.dispose()
    environmentShader.dispose()
    physics.dispose()
  }
}
