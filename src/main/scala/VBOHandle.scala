package VBOHandlle

import org.lwjgl.opengl.GL15

case class VBOHandle(handle: Int, bufType: Int) {
  def bind[T](body: => T): Unit = {
    GL15 glBindBuffer(bufType, handle)
    try body finally GL15 glBindBuffer(bufType, 0)
  }

  def release(): Unit = {
    GL15 glBindBuffer(bufType, 0)
    GL15 glDeleteBuffers handle
  }
}