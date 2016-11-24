package galenscovell.soulslite.processing

import com.badlogic.ashley.core._
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.physics.box2d._
import galenscovell.soulslite.actors.components._
import galenscovell.soulslite.actors.components.dynamic.ColorComponent
import galenscovell.soulslite.actors.systems._
import galenscovell.soulslite.ui.screens.GameScreen


class EntityManager(engine: Engine, spriteBatch: SpriteBatch, controllerHandler: GameController, world: World, gameScreen: GameScreen) {
  setupSystems()


  private def setupSystems(): Unit = {
    // Handles entity position and velocity
    val movementSystem: MovementSystem = new MovementSystem(
      Family.all(
        classOf[BodyComponent],
        classOf[VelocityComponent]
      ).get()
    )

    // Handles player input
    val inputSystem: PlayerControlSystem = new PlayerControlSystem(
      Family.all(
        classOf[BodyComponent],
        classOf[PlayerComponent],
        classOf[VelocityComponent],
        classOf[WeaponComponent]
      ).get(), controllerHandler
    )

    // Handles combat collisions and effects
    val collisionSystem: CollisionSystem = new CollisionSystem(
      Family.all(
        classOf[BodyComponent],
        classOf[WeaponComponent]
      ).get(), world
    )

    // Handles entity graphics
    val renderSystem: RenderSystem = new RenderSystem(
      Family.all(
        classOf[AnimationComponent],
        classOf[BodyComponent],
        classOf[RenderableComponent],
        classOf[SizeComponent],
        classOf[SpriteComponent],
        classOf[VelocityComponent]
      ).get(), spriteBatch, gameScreen
    )

    engine.addSystem(movementSystem)
    engine.addSystem(inputSystem)
    engine.addSystem(collisionSystem)
    engine.addSystem(renderSystem)
  }

  def makeEntity(player: Boolean, etype: String, size: Float, posX: Float, posY: Float): Entity = {
    val e: Entity = new Entity
    val bodyComponent: BodyComponent = new BodyComponent(e, world, posX, posY, size)
    e.add(new AnimationComponent(etype))
    e.add(bodyComponent)
    e.add(new RenderableComponent)
    e.add(new SizeComponent(size))
    e.add(new SpriteComponent(etype))
    e.add(new VelocityComponent)
    e.add(new WeaponComponent(world, bodyComponent.body))

    if (player) {
      e.add(new PlayerComponent)
    }

    engine.addEntity(e)
    e
  }

  def update(delta: Float): Unit = {
    engine.update(delta)
  }
}
