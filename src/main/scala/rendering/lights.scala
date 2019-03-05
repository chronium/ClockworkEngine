package rendering

import org.joml.Vector3f

class Attenuation(val constant: Float, val linear: Float, val exponent: Float)

class PointLight(val color: Vector3f, val position: Vector3f, val intensity: Float, val att: Attenuation)
