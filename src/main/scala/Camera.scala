package Camera

import Transform.Transform
import org.joml.Vector3f

class Camera {
  val transform: Transform = Transform.Identity

  def move(offset: Vector3f): Unit = {
    if (offset.z != 0) {
      transform.position.x += Math.sin(Math.toRadians(transform.rotation.y)).toFloat * -1.0f * offset.z
      transform.position.z += Math.cos(Math.toRadians(transform.rotation.y)).toFloat * offset.z
    }

    if (offset.x != 0) {
      transform.position.x += Math.sin(Math.toRadians(transform.rotation.y - 90)).toFloat * -1.0f * offset.x
      transform.position.z += Math.cos(Math.toRadians(transform.rotation.y - 90)).toFloat * offset.x
    }

    transform.position.y += offset.y
  }
}
