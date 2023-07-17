import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.application.Application.launch
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.stage.Stage
import javafx.util.Duration

class SpaceInvaders : Application(){

    //windows size
    private val gameWidth = 1000.0
    private val gameHeight = 750.0
    private val enemyWidth = 40.0

    private val enemyHeight = 30.0
    private val enemySpeed = 3.0
    private val enemyBulletSpeed = 4.0

    private val playerSpeed = 5.0
    private val playerBulletSpeed = 10.0

    private val enemyRows = 5
    private val enemyCols = 10

    private val enemyFireInterval = 1000L
    private var enemyLastFireTime = 0L

    private var stared = false
    private lateinit var player: Player
    private lateinit var enemies: MutableList<Enemy>
    private var playerBullets: MutableList<PlayerBullet> = mutableListOf()
    private var enemyBullets: MutableList<EnemyBullet> = mutableListOf()
    private var enemyImageUrls = mutableListOf<String>()
    private var enemyBulletUrls = mutableListOf<String>()
    private lateinit var playerBulletSound: MediaPlayer
    private lateinit var enemyDestroySound: MediaPlayer
    private lateinit var playerExplosionSound: MediaPlayer
    private lateinit var gameLoop: AnimationTimer

    private val enemyScores = arrayOf(30.0, 20.0, 10.0)

    private var level = 1
    private var score = 0.0
    private var lives = 3

    private lateinit var scoreText: Text
    private lateinit var liveText: Text
    private lateinit var levelText: Text


    private var moveDirection = Enemy.Direction.RIGHT
    private val spacingDistance = 5.0

    override fun start(primaryStage: Stage) {

        enemyImageUrls += SpaceInvaders::class.java.getResource("/images/enemy1.png")!!.toString()
        enemyImageUrls += SpaceInvaders::class.java.getResource("/images/enemy2.png")!!.toString()
        enemyImageUrls += SpaceInvaders::class.java.getResource("/images/enemy3.png")!!.toString()

        enemyBulletUrls += SpaceInvaders::class.java.getResource("/images/bullet1.png")!!.toString()
        enemyBulletUrls += SpaceInvaders::class.java.getResource("/images/bullet2.png")!!.toString()
        enemyBulletUrls += SpaceInvaders::class.java.getResource("/images/bullet3.png")!!.toString()

        primaryStage.title = "Space Invaders"
        primaryStage.scene = createInstructionScene(primaryStage, gameWidth, gameHeight)
        primaryStage.show()


    }

    // create initial scene
    private fun createInstructionScene(stage: Stage, width: Double, height: Double) : Scene{

        val borderPane = BorderPane()
        val hBox = HBox()
        val vBox1 = VBox()
        val vBox2 = VBox()

        val image = Image(SpaceInvaders::class.java.getResource("/images/logo.png")!!.toString())
        val imageView = ImageView(image)

        val labelInstruction = Label("Instruction")
        val labelStart = Label("Enter -Start Game")
        val labelMove = Label("A or ◀,D or ▶ -Move ship left or right")
        val labelFire = Label("SPACE -Fire!")
        val labelQuit = Label("Q -Quit Game")
        val labelLevel = Label("1 or 2 or 3 -Start Game at Specific level")

        val styleInstruction = "-fx-font-weight: bold; -fx-font-size: 24;"
        val styleOperation = "-fx-font-size: 16;"
        val labelInformation = Label("Your name!")

        // label text style
        labelInstruction.style = styleInstruction
        labelStart.style = styleOperation
        labelMove.style = styleOperation
        labelFire.style = styleOperation
        labelQuit.style = styleOperation
        labelLevel.style =styleOperation


        // borderPane layout
        hBox.alignment = Pos.CENTER
        hBox.children.add(imageView)

        vBox1.alignment = Pos.CENTER
        vBox1.spacing = 15.0
        vBox1.children.addAll(labelStart, labelMove, labelFire, labelQuit, labelLevel)

        vBox2.alignment = Pos.CENTER
        vBox2.spacing = 40.0
        vBox2.children.addAll(labelInstruction, vBox1)

        borderPane.top = hBox
        borderPane.center = vBox2
        borderPane.bottom = labelInformation

        val scene = Scene(borderPane, width, height)

        scene.setOnKeyPressed { event ->
            // press enter and the game didn't start
            if (event.code == KeyCode.ENTER && !stared) {
                stared = true
                stage.scene = createGameScene(width, height, stage)
            }
            // quit game
            else if (event.code == KeyCode.Q) {
                Platform.exit()
            }
            else if (event.code == KeyCode.DIGIT1 || event.code == KeyCode.NUMPAD1) {
                level = 1
                stared = true
                stage.scene = createGameScene(width, height, stage)
            }
            else if (event.code == KeyCode.DIGIT2 || event.code == KeyCode.NUMPAD2) {
                level = 2
                stared = true
                stage.scene = createGameScene(width, height, stage)
            }
            else if (event.code == KeyCode.DIGIT3 || event.code == KeyCode.NUMPAD3) {
                level = 3
                stared = true
                stage.scene = createGameScene(width, height, stage)
            }
        }


        return scene

    }

    private fun createGameScene(width: Double, height: Double, stage: Stage): Scene {

        val root = Group()
        val scene = Scene(root, width, height)
        val canvas = Canvas(width, height)
        val gc = canvas.graphicsContext2D
        playerBulletSound = MediaPlayer(Media(SpaceInvaders::class.java.getResource("/sounds/shoot.wav")?.toString()))
        enemyDestroySound = MediaPlayer(Media(SpaceInvaders::class.java.getResource("/sounds/invaderkilled.wav")?.toString()))
        playerExplosionSound = MediaPlayer(Media(SpaceInvaders::class.java.getResource("/sounds/explosion.wav")?.toString()))
        player = Player(width / 2, height - 50, playerSpeed, width)
        enemies = createEnemies()

        scoreText = createInformationText(0.0, 0.0, "Score: $score")
        levelText = createInformationText(gameWidth-60.0, 0.0, "Level: $level")
        liveText = createInformationText(gameWidth-150.0, 0.0, "Lives: $lives")

        root.children.addAll(canvas, scoreText, liveText, levelText)
        // set filled color
        scene.fill = Color.BLACK

        scene.setOnKeyPressed { event ->

            when (event.code) {
                KeyCode.LEFT, KeyCode.A -> player.moveLeft()
                KeyCode.RIGHT, KeyCode.D -> player.moveRight()
                KeyCode.SPACE ->
                {playerBullets.add(PlayerBullet(player.x + player.playerWidth/2.0, player.y, playerBulletSpeed, SpaceInvaders::class.java.getResource("/images/player_bullet.png")!!
                    .toString()))
                    playerBulletSound.seek(Duration.ZERO)
                    playerBulletSound.play()
                }


                else -> {}
            }
        }

        scene.setOnKeyReleased { event->
            when (event.code) {
                KeyCode.LEFT, KeyCode.RIGHT, KeyCode.A, KeyCode.D -> player.stopMoving()
                else -> {}
            }
        }

        val gameLoop = object : AnimationTimer() {
            override fun handle(now: Long){
                update()
                render(gc)
                val enemyReachBoundary = enemies.any { enemy ->
                    enemy.y >= gameHeight - 40.0
                }

                if (enemies.size == 0 && level < 3 && lives > 0) {
                    pauseGameLoop()
                    level += 1
                    stage.scene = createGameScene(width, height,  stage)
                }
                else if (enemies.size == 0 && level == 3 || lives == 0) {
                    pauseGameLoop()
                    val result = if(lives == 0)(false) else true
                    createResultStage(result, score, stage)
                }
                else if (enemyReachBoundary) {
                    pauseGameLoop()
                    val result = false
                    createResultStage(result, score, stage)
                }
            }
        }

        this.gameLoop = gameLoop
        this.gameLoop.start()
        return scene

    }

    // update all items' position
    private fun update() {
        player.update()

        enemies.forEach { enemy ->
            enemy.update(moveDirection)
        }
        enemyBullets.forEach{enemyBullet ->
            enemyBullet.update()
        }

        // check if reached boundary
        val reachedBoundary = enemies.any { enemy ->
            enemy.x <= 0 || enemy.x + enemy.enemyWidth >= gameWidth
        }

        if (reachedBoundary) {
            moveDirection = if (moveDirection == Enemy.Direction.RIGHT) {
                Enemy.Direction.LEFT
            } else {
                Enemy.Direction.RIGHT
            }

            enemies.forEach { enemy ->
                enemy.moveDown()
            }
        }

        val iteratorPlayerBullet = playerBullets.iterator()
        while (iteratorPlayerBullet.hasNext()) {
            val bullet = iteratorPlayerBullet.next()
            bullet.update()

            // Check if the player bullet hits an enemy
            val hitEnemy = checkCollision(bullet)
            if (hitEnemy != null) {
                // play sound
                enemyDestroySound.stop()
                enemyDestroySound.seek(Duration.ZERO)
                enemyDestroySound.play()
                // update score
                score += hitEnemy.enemyScore
                scoreText.text = "Score: $score"

                // Remove the hit enemy
                enemies.remove(hitEnemy)
                // Remove the hit bullet
                iteratorPlayerBullet.remove()
            }
        }

        // Check if it's time for an enemy to fire a bullet
        val currentTime = System.currentTimeMillis()
        if (currentTime - enemyLastFireTime >= enemyFireInterval) {
            val randomEnemy = enemies.shuffled().firstOrNull()
            randomEnemy?.let { enemy ->
                val enemyBullet = EnemyBullet(
                    enemy.x + enemy.enemyWidth / 2.0,
                    enemy.y + enemy.enemyHeight,
                    enemyBulletSpeed,
                    enemyBulletUrls[enemy.type]
                )
                enemyBullets.add(enemyBullet)
                enemyLastFireTime = currentTime
            }
        }

        val iteratorEnemyBullet = enemyBullets.iterator()
        while (iteratorEnemyBullet.hasNext()) {
            val bullet = iteratorEnemyBullet.next()
            bullet.update()

            // Check if the player bullet hits the ship
            val hitPlayer = bullet.intersects(player)
            if (hitPlayer) {
                // play sound
                playerExplosionSound.stop()
                playerExplosionSound.seek(Duration.ZERO)
                playerExplosionSound.play()
                lives -= 1

                liveText.text = "Lives: $lives"

                iteratorEnemyBullet.remove()
            }
        }


    }

    // draw items
    private fun render(gc: GraphicsContext) {
        gc.clearRect(0.0, 0.0, gameWidth, gameHeight)
        player.render(gc)
        enemies.forEach{ enemy->
            enemy.render(gc)
        }
        playerBullets.removeIf{playerBullet ->
            playerBullet.y <= 0
        }
        playerBullets.forEach{playerBullet ->
            playerBullet.render(gc)
        }

       enemyBullets.removeIf{enemyBullet ->
            enemyBullet.y >= gameWidth
        }
        enemyBullets.forEach{enemyBullet ->
            enemyBullet.render(gc)
        }
    }

    private fun createEnemies(): MutableList<Enemy> {
        val enemies = mutableListOf<Enemy>()

        repeat(enemyRows) {i ->
            repeat(enemyCols){j ->
                val enemy = Enemy(j*(enemyWidth + spacingDistance) + 15.0, (enemyRows-i)*(enemyHeight + spacingDistance) + 15.0, enemySpeed*level, enemyImageUrls[i/2], enemyScores[(enemyRows-i)/2], i/2)
                enemies += enemy
            }

        }

        return enemies
    }

    private fun createInformationText(x: Double, y:Double, type: String): Text {

        val informationText = Text()
        informationText.font = Font.font("Arial", FontWeight.BOLD, 16.0)
        informationText.fill = Color.WHITE
        informationText.textOrigin = VPos.TOP
        informationText.text = type
        informationText.x = x
        informationText.y = y
        return informationText


    }

    private fun createResultStage(result: Boolean, score: Double, primaryStage: Stage) {

        val stage = Stage()
        val vBox1 = VBox()
        val vBox2 = VBox()
        val vBox3 = VBox()
        val labelGameOver = Label()
        val labelResult = Label()
        val labelScore = Label()
        val labelQuit = Label()
        val labelRestart = Label()
        val labelInstruction = Label()
        val labelLevel = Label()

        if (result) {
            labelResult.text = "You Win!"
        } else {
            labelResult.text = "You lose!"
        }

        labelGameOver.text = "GAME OVER!"
        labelScore.text = "Your Score: $score"
        labelRestart.text = "ENTER -Start New Game"
        labelInstruction.text = "I -Back to Instruction"
        labelQuit.text = "Q -Quit Game"
        labelLevel.text = "1 or 2 or 3 -Start Game at Specific level"


        val styleGameOver = "-fx-font-weight: bold; -fx-font-size: 30;"
        val styleResult = "-fx-font-weight: bold; -fx-font-size: 24;"
        val styleOperation = "-fx-font-size: 16;"

        labelGameOver.style = styleGameOver
        labelResult.style = styleResult
        labelScore.style = styleOperation
        labelQuit.style = styleOperation
        labelRestart.style = styleOperation
        labelLevel.style = styleOperation
        labelInstruction.style = styleOperation

        vBox1.children.addAll(labelGameOver, labelResult)
        vBox1.spacing = 15.0
        vBox1.alignment = Pos.CENTER

        vBox2.children.addAll(labelScore, labelInstruction, labelQuit, labelRestart, labelLevel)
        vBox2.spacing = 15.0
        vBox2.alignment = Pos.CENTER

        vBox3.children.addAll(vBox1, vBox2)
        vBox3.spacing = 30.0
        vBox3.alignment = Pos.CENTER

        val scene = Scene(vBox3, 400.0, 300.0)
        scene.setOnKeyPressed { event ->
            when (event.code) {
                KeyCode.Q ->{
                    Platform.exit()
                }

                KeyCode.ENTER, KeyCode.NUMPAD1, KeyCode.DIGIT1,  KeyCode.NUMPAD2, KeyCode.DIGIT2,  KeyCode.NUMPAD3, KeyCode.DIGIT3 ->{
                    if(event.code ==  KeyCode.ENTER || event.code == KeyCode.NUMPAD1 || event.code == KeyCode.DIGIT1){
                        level = 0
                    }
                    else if(event.code == KeyCode.NUMPAD2 || event.code == KeyCode.DIGIT2){
                        level = 1
                    }
                    else if(event.code == KeyCode.NUMPAD3 || event.code == KeyCode.DIGIT3){
                        level = 2
                    }
                    primaryStage.scene = createGameScene(gameWidth, gameHeight, primaryStage)
                    stared = false
                    this.score = 0.0
                    this.lives = 3
                    this.enemies.clear()
                    this.playerBullets.clear()
                    this.enemyBullets.clear()

                    scoreText.text = "Score: $score"
                    liveText.text = "Lives: $lives"
                    levelText.text = "Level: $level"

                    stage.close()
                }

                KeyCode.I ->{
                    primaryStage.scene = createInstructionScene(primaryStage, gameWidth, gameHeight)
                    stared = false
                    this.score = 0.0
                    this.level = 1
                    this.lives = 3
                    this.enemies.clear()
                    this.playerBullets.clear()
                    this.enemyBullets.clear()
                    stage.close()
                }
                else -> {}
            }
        }

        stage.scene = scene
        stage.title = "result"
        stage.show()
    }

    private fun checkCollision(bullet: PlayerBullet): Enemy? {
        for (enemy in enemies) {
            if (bullet.intersects(enemy)) {
                return enemy
            }
        }
        return null
    }


    private fun pauseGameLoop() {
        gameLoop.stop() // 停止游戏循环
    }


}

fun main() {
    launch(SpaceInvaders::class.java)
}