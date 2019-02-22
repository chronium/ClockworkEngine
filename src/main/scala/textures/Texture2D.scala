package textures

import org.lwjgl.opengl.GL11

class Texture2D {

}

case class Texture2DHandle(handle: Int, target: Int) {
  def bind[T](body: => T): Unit = {
    GL11 glBindTexture(target, handle)
    try body finally GL11 glBindTexture(target, 0)
  }

  def release(): Unit = {
    GL11 glBindTexture(target, 0)
    GL11 glDeleteTextures handle
  }
}
