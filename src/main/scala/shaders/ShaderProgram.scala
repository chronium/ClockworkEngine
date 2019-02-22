package shaders

import org.lwjgl.opengl
import org.lwjgl.opengl.{GL11, GL20}

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
    GL20 glUniform1i(handle, value)
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
