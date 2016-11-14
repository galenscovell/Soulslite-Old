package galenscovell.soulslite.ui.screens

import com.badlogic.gdx.scenes.scene2d._
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui._
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import galenscovell.soulslite.Main
import galenscovell.soulslite.util._


class MainMenuScreen(root: Main) extends AbstractScreen(root) {


  override def create(): Unit = {
    super.create()

    val mainTable: Table = new Table
    mainTable.setFillParent(true)

    val titleTable: Table = new Table
    val titleLabel: Label = new Label("Soulslite", Resources.labelXLargeStyle)
    titleLabel.setAlignment(Align.center, Align.left)
    titleTable.add(titleLabel).width(Constants.UI_X * 0.75f).height(Constants.UI_Y / 6)

    val buttonTable: Table = new Table
    val newGameTable: Table = new Table
    newGameTable.setBackground(Resources.npDarkGray)
    val newGameButton: TextButton = new TextButton("New Game", Resources.buttonMenuStyle)
    newGameButton.getLabel.setAlignment(Align.center, Align.center)
    newGameButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        root.createGameScreen()
        stage.getRoot.addAction(Actions.sequence(
          Actions.fadeOut(0.3f),
          toStartScreenAction)
        )
      }
    })
    val continueGameTable: Table = new Table
    continueGameTable.setBackground(Resources.npDarkGray)
    val continueGameButton: TextButton = new TextButton("Continue Game", Resources.buttonMenuStyle)
    continueGameButton.getLabel.setAlignment(Align.center, Align.center)
    continueGameButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {

      }
    })
    val settingTable: Table = new Table
    settingTable.setBackground(Resources.npDarkGray)
    val settingButton: TextButton = new TextButton("Preferences", Resources.buttonMenuStyle)
    settingButton.getLabel.setAlignment(Align.center, Align.center)
    settingButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {

      }
    })
    val quitTable: Table = new Table
    quitTable.setBackground(Resources.npDarkGray)
    val quitButton: TextButton = new TextButton("Close", Resources.buttonMenuStyle)
    quitButton.getLabel.setAlignment(Align.center, Align.center)
    quitButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        stage.getRoot.addAction(Actions.sequence(
          Actions.fadeOut(0.3f),
          quitGameAction)
        )
      }
    })

    val detailTable: Table = new Table
    val detailLabel: Label = new Label(s"v1a 2016 Galen Scovell", Resources.labelSmallStyle)
    detailLabel.setAlignment(Align.center, Align.right)
    detailTable.add(detailLabel).width(Constants.UI_X * 0.75f).height(Constants.UI_Y * 0.15f)

    buttonTable.add(newGameButton).width(Constants.UI_X * 0.75f).height(Constants.UI_Y * 0.15f).pad(6).left
    buttonTable.add(newGameTable).width(Constants.UI_X * 0.22f).height(Constants.UI_Y * 0.15f).expand.pad(6).right
    buttonTable.row
    buttonTable.add(continueGameTable).width(Constants.UI_X * 0.22f).height(Constants.UI_Y * 0.15f).expand.pad(6).left
    buttonTable.add(continueGameButton).width(Constants.UI_X * 0.75f).height(Constants.UI_Y * 0.15f).pad(6).right
    buttonTable.row
    buttonTable.add(settingButton).width(Constants.UI_X * 0.75f).height(Constants.UI_Y * 0.15f).pad(6).left
    buttonTable.add(settingTable).width(Constants.UI_X * 0.22f).height(Constants.UI_Y * 0.15f).expand.pad(6).right
    buttonTable.row
    buttonTable.add(quitTable).width(Constants.UI_X * 0.22f).height(Constants.UI_Y * 0.15f).expand.pad(6).left
    buttonTable.add(quitButton).width(Constants.UI_X * 0.75f).height(Constants.UI_Y * 0.15f).pad(6).right

    mainTable.add(titleTable).width(Constants.UI_X).height(Constants.UI_Y * 0.1f).expand.center.pad(6)
    mainTable.row
    mainTable.add(buttonTable).width(Constants.UI_X).height(Constants.UI_Y * 0.65f).expand.center.pad(6)
    mainTable.row
    mainTable.add(detailTable).width(Constants.UI_X).height(Constants.UI_Y * 0.1f).expand.center.pad(6)

    stage.addActor(mainTable)
    mainTable.addAction(Actions.sequence(
      Actions.fadeOut(0),
      Actions.fadeIn(0.3f))
    )
  }


  /***************************
    * Custom Scene2D Actions *
    ***************************/
  private[screens] var toStartScreenAction: Action = (delta: Float) => {
    root.setScreen(root.gameScreen)
    true
  }
  private[screens] var quitGameAction: Action = (delta: Float) => {
    root.quitGame()
    true
  }
}
