import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image

class EnemyBullet(
    private var x: Double,
    var y:Double,
    private var speed: Double,
    private var url: String

) {

    // enemies' bullet
    private val enemyBullet = Image(url)
    private val enemyBulletWidth = 10.0
    private val enemyBulletHeight = 20.0

    fun update() {
        y += speed
    }

    fun render(gc: GraphicsContext) {
        gc.drawImage(enemyBullet, x, y, enemyBulletWidth, enemyBulletHeight)

    }

    fun intersects(player: Player): Boolean {
        val bulletRight = x + enemyBulletWidth
        val bulletBottom = y + enemyBulletHeight
        val playerRight = player.x + player.playerWidth
        val playerBottom = player.y +player.playerHeight

        return x < playerRight && bulletRight > player.x && y < player.y && bulletBottom > player.y
    }
}