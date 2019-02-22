import org.joml.{Matrix4f, Vector3f}

object Transform {
  var FOV: Float = Math toRadians 60.0 toFloat
  val Z_NEAR: Float = 0.001f
  val Z_FAR: Float = 1000.0f
}

case class Transform(position: Vector3f = new Vector3f(0), scale: Vector3f = new Vector3f(1), rotation: Vector3f = new Vector3f(0)) {
  def worldMatrix: Matrix4f =
    new Matrix4f().translate(position)
      .rotateX(Math toRadians (rotation.x toDouble) toFloat)
      .rotateY(Math toRadians (rotation.y toDouble) toFloat)
      .rotateZ(Math toRadians (rotation.z toDouble) toFloat)
      .scale(scale)
}
