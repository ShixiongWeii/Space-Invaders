import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import kotlin.math.max
import kotlin.math.min

class Player(var x: Double, var y: Double, private var speed: Double, private val gameWidth: Double) {

    private var movingLeft = false
    private var movingRight = false
    private val playerImage = Image(SpaceInvaders::class.java.getResource("/images/player.png")!!.toString())
    val playerWidth = 40.0
    val playerHeight = 30.0


    fun moveLeft() {
        movingLeft = true
    }

    fun moveRight() {
        movingRight = true
    }

    fun stopMoving() {
        movingLeft = false
        movingRight = false
    }

    fun update() {
        if (movingLeft) {
            x -= speed
            x = max(x, 0.0)
        }
        else if (movingRight) {
            x += speed
            x = min(x, gameWidth-playerWidth)
        }
    }

    fun render(gc: GraphicsContext) {
        gc.drawImage(playerImage, x, y, this.playerWidth, this.playerHeight)
    }




}