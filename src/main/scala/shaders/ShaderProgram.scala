package shaders

import java.nio.FloatBuffer

import org.joml.{Matrix4f, Vector3f, Vector4f}
import org.lwjgl.system.MemoryStack._
import org.lwjgl.opengl.{GL11, GL20}
import rendering.{Attenuation, PointLight}

import scala.collection.mutable
import scala.io.Source

case class ShaderProgram(handle: Int) {
  var shaders: Array[Int] = Array[Int]()

  def attachShader(shaderHandle: Int): this.type = {
    GL20 glAttachShader(handle, shaderHandle)
    shaders = shaders :+ shaderHandle
    this
  }

  def attachShaders(handles: Seq[Int]): this.type = {
    for (handle <- handles)
      attachShader(handle)
    this
  }

  def build: ShaderProgramHandle = {
    GL20 glLinkProgram handle
    if (GL20.glGetProgrami(handle, GL20 GL_LINK_STATUS) == GL11.GL_FALSE) {
      throw new RuntimeException("Could not link program: " ++ GL20.glGetProgramInfoLog(handle, 1024))
    }

    shaders.foreach(shader => GL20 glDetachShader(handle, shader))

    GL20 glValidateProgram handle
    if (GL20.glGetProgrami(handle, GL20 GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
      throw new RuntimeException("Could not validate program: " ++ GL20.glGetProgramInfoLog(handle, 1024))
    }

    ShaderProgramHandle(shaders, handle)
  }

  def bindAttribLocation(position: Int, attrib: String): this.type = {
    GL20 glBindAttribLocation(handle, position, attrib)
    this
  }

  def bindAttribLocations(attribs: (Int, String)*): this.type = {
    for (attrib <- attribs)
      bindAttribLocation(attrib._1, attrib._2)
    this
  }
}

case class ShaderProgramHandle(shaders: Array[Int], handle: Int) {
  val uniforms: mutable.HashMap[String, Int] = new mutable.HashMap[String, Int]()

  def bind[T](body: => T): Unit = {
    GL20 glUseProgram handle
    try body finally GL20 glUseProgram 0
  }

  def release[T](): Unit = {
    GL20 glUseProgram 0

    shaders.foreach(shader => GL20 glDeleteShader shader)
    GL20 glDeleteProgram handle
  }

  def setUniform(uniform: String, value: Int): Unit = {
    if (!uniforms.contains(uniform))
      uniforms += (uniform -> GL20.glGetUniformLocation(handle, uniform))
    GL20 glUniform1i(uniforms(uniform), value)
  }

  def setUniform(uniform: String, value: Float): Unit = {
    if (!uniforms.contains(uniform))
      uniforms += (uniform -> GL20.glGetUniformLocation(handle, uniform))
    GL20 glUniform1f(uniforms(uniform), value)
  }

  def setUniform(uniform: String, value: Vector4f): Unit = {
    if (!uniforms.contains(uniform))
      uniforms += (uniform -> GL20.glGetUniformLocation(handle, uniform))

    GL20 glUniform4f(uniforms(uniform), value.x, value.y, value.z, value.w)
  }

  def setUniform(uniform: String, value: Vector3f): Unit = {
    if (!uniforms.contains(uniform))
      uniforms += (uniform -> GL20.glGetUniformLocation(handle, uniform))

    GL20 glUniform3f(uniforms(uniform), value.x, value.y, value.z)
  }

  def setUniform(uniform: String, value: Attenuation): Unit = {
    setUniform(s"$uniform.constant", value.constant)
    setUniform(s"$uniform.linear", value.linear)
    setUniform(s"$uniform.exponent", value.exponent)
  }

  def setUniform(uniform: String, value: PointLight): Unit = {
    setUniform(s"$uniform.color", value.color)
    setUniform(s"$uniform.position", value.position)
    setUniform(s"$uniform.intensity", value.intensity)
    setUniform(s"$uniform.att", value.att)
  }

  def setUniform(uniform: String, value: Matrix4f): Unit = {
    if (!uniforms.contains(uniform))
      uniforms += (uniform -> GL20.glGetUniformLocation(handle, uniform))

    val stack = stackPush

    val fb: FloatBuffer = stack mallocFloat 16
    value.get(fb)

    GL20 glUniformMatrix4fv(uniforms(uniform), false, fb)

    stackPop
  }
}

trait ShaderType {
  def loadShader(shaderType: Int)(path: String): Int = {
    val handle = GL20 glCreateShader shaderType
    val source = Source fromFile path

    GL20 glShaderSource(handle, source mkString)
    GL20 glCompileShader handle

    source close()

    if (GL20.glGetShaderi(handle, GL20 GL_COMPILE_STATUS) == GL11.GL_FALSE)
      throw new RuntimeException(s"Could not compile shader $path\n" ++ GL20.glGetShaderInfoLog(handle, 1024))

    handle
  }

  def getGLType: Int

  def |:| : String => Int = this loadShader getGLType
}

case object VertexShader extends ShaderType {
  override def getGLType: Int = GL20 GL_VERTEX_SHADER
}

case object FragmentShader extends ShaderType {
  override def getGLType: Int = GL20 GL_FRAGMENT_SHADER
}

trait ShaderTrait {
  def createShader(): ShaderProgram = ShaderProgram(GL20 glCreateProgram)

  def apply(shaders: Int*): ShaderProgram = {
    val handle = createShader()
    handle attachShaders shaders
    handle
  }
}

object ShaderProgram extends ShaderTrait
