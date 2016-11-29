package galenscovell.soulslite.actors.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import galenscovell.soulslite.processing.BaseSteerable


class WhereIsPlayerComponent(playerSteerable: BaseSteerable) extends Component {
  var distanceFromPlayer: Vector2 = new Vector2()


  def getPlayerPosition: Vector2 = {
    playerSteerable.getBody.getPosition
  }

  def getPlayerSteerable: BaseSteerable = {
    playerSteerable
  }
}
