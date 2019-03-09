package rendering

import org.joml.{Matrix4f, Vector3f, Vector4f}

class Attenuation(val constant: Float, val linear: Float, val exponent: Float)

class PointLight(val color: Vector3f, val position: Vector3f, val intensity: Float, val att: Attenuation) {
  def intoView(view: Matrix4f): PointLight = {
    val light = new PointLight(this.color, new Vector3f(this.position), this.intensity, this.att)
    val aux = new Vector4f(this.position, 1)
    aux.mul(view)
    light.position.x = aux.x
    light.position.y = aux.y
    light.position.z = aux.z
    light
  }
}
