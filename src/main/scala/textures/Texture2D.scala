package textures

import java.nio.{ByteBuffer, IntBuffer}

import org.lwjgl.system.MemoryStack._
import org.lwjgl.opengl.{GL11, GL12, GL13, GL45}
import org.lwjgl.stb.STBImage._

case class TextureHandle(width: Int, height: Int)(handle: Int, target: Int) {
  def bind[T](body: => T): Unit = {
    GL13 glActiveTexture (GL13 GL_TEXTURE0)
    GL11 glBindTexture(target, handle)
    try body finally GL11 glBindTexture(target, 0)
  }

  def bind[T](texture: Int)(body: => T): Unit = {
    GL13 glActiveTexture ((GL13 GL_TEXTURE0) + texture)
    GL11 glBindTexture(target, handle)
    try body finally GL11 glBindTexture(target, 0)
  }

  def release(): Unit = {
    GL11 glBindTexture(target, 0)
    GL11 glDeleteTextures handle
  }
}

object Texture2D {
  def createTexture2D(path: String): TextureHandle = {
    val handle = GL45 glCreateTextures GL11.GL_TEXTURE_2D
    val (rgba, width, height) = loadImage(path)

    GL45 glTextureStorage2D(handle, 6, GL11 GL_RGBA8, width, height)
    GL45 glTextureSubImage2D(handle, 0, 0, 0, width, height, GL11 GL_RGBA, GL11 GL_UNSIGNED_BYTE, rgba)

    GL45 glGenerateTextureMipmap handle

    GL45 glTextureParameteri(handle, GL11 GL_TEXTURE_MIN_FILTER, GL11 GL_LINEAR_MIPMAP_LINEAR)
    GL45 glTextureParameteri(handle, GL11 GL_TEXTURE_MAG_FILTER, GL11 GL_LINEAR)

    GL45 glTextureParameteri(handle, GL11 GL_TEXTURE_WRAP_S, GL12 GL_CLAMP_TO_EDGE)
    GL45 glTextureParameteri(handle, GL11 GL_TEXTURE_WRAP_T, GL12 GL_CLAMP_TO_EDGE)

    TextureHandle(width, height)(handle, GL11 GL_TEXTURE_2D)
  }

  def loadImage(path: String): (ByteBuffer, Int, Int) = {
    val stack = stackPush
    val w: IntBuffer = stack mallocInt 1
    val h: IntBuffer = stack mallocInt 1
    val c: IntBuffer = stack mallocInt 1

    stbi_set_flip_vertically_on_load(true)
    val image = stbi_load(path, w, h, c, 4)

    if (image == null)
      throw new RuntimeException(s"Failed to load image!\n$stbi_failure_reason")

    val width = w get
    val height = h get

    stack pop

    (image, width, height)
  }
}
