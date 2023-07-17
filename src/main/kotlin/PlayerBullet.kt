import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image

class PlayerBullet(
    private var x: Double,
    var y: Double,
    private var speed: Double,
    private var url: String
) {
    private val playerBulletImage = Image(url)
    private val playerBulletWidth = 5.0
    private val playerBulletHeight = 20.0

    fun update() {
        y -= speed
    }

    fun render(gc: GraphicsContext) {
        gc.drawImage(playerBulletImage, x, y, playerBulletWidth, playerBulletHeight)
    }

    fun intersects(enemy: Enemy): Boolean {
        val bulletRight = x + playerBulletWidth
        val bulletBottom = y + playerBulletHeight
        val enemyRight = enemy.x + enemy.enemyWidth
        val enemyBottom = enemy.y +enemy.enemyHeight

        return x < enemyRight && bulletRight > enemy.x && y < enemy.y && bulletBottom > enemy.y
    }


}