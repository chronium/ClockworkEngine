package VAOHandle

import org.lwjgl.opengl.GL30

case class VAOHandle(handle: Int) {
  def bind[T](body: => T): Unit = {
    GL30 glBindVertexArray handle
    try body finally GL30 glBindVertexArray 0
  }

  def release(): Unit = {
    GL30 glBindVertexArray 0
    GL30 glDeleteVertexArrays handle
  }
}