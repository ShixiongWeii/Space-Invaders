import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image

class Enemy(
    var x: Double,
    var y: Double,
    private var speed: Double,
    private val url: String,
    val enemyScore: Double,
    val type: Int
) {

    private val enemyImage = Image(url)
    private val moveDownDistance = 15.0
    val enemyWidth = 30.0
    val enemyHeight = 25.0
    private var moveDownNum = 0    //count the number of enemy drops

    fun update(direction: Direction) {
        x += if (direction == Direction.RIGHT) (speed + moveDownNum/4) else -(speed + moveDownNum/4)
    }

    fun moveDown() {
        moveDownNum += 1
        y += moveDownDistance
    }

    fun render(gc: GraphicsContext) {
        gc.drawImage(enemyImage, x, y, enemyWidth, enemyHeight)
    }

    enum class Direction {
        LEFT,
        RIGHT
    }




}