import java.nio.{FloatBuffer, ShortBuffer}

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.{GL11, GL15, GL20, GL45}

case class Model[A, B](buffer: FloatBuffer, indices: Array[Short])(setAttribs: => A)(enableAttribs: => B) {
  val indicesCount: Int = indices length
  val indexBuffer: ShortBuffer = BufferUtils createShortBuffer indices.length
  indexBuffer put indices flip

  val vao: VAOHandle = Model.newVAO
  val vbo: VBOHandle = Model.newVBO(GL15 GL_ARRAY_BUFFER)
  val vboi: VBOHandle = Model.newVBO(GL15 GL_ELEMENT_ARRAY_BUFFER)

  vao bind {
    vbo bind {
      GL15 glBufferData(GL15 GL_ARRAY_BUFFER, buffer, GL15 GL_STATIC_DRAW)
      setAttribs
    }
  }

  vboi bind GL15.glBufferData(GL15 GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15 GL_STATIC_DRAW)

  def render(): Unit = {
    vao bind {
      vboi bind {
        enableAttribs
        GL11 glDrawElements(GL11 GL_TRIANGLES, indicesCount, GL11 GL_UNSIGNED_SHORT, 0)
      }
    }
  }
}

class VertexColorModel(buffer: FloatBuffer, indices: Array[Short]) extends Model[Unit, Unit](buffer, indices)({
  ColoredVertex setPositionAttribPointer 0
  ColoredVertex setColorAttribPointer 1
})({
  GL20 glEnableVertexAttribArray 0
  GL20 glEnableVertexAttribArray 1
})

class VertexColorTextureModel(buffer: FloatBuffer, indices: Array[Short]) extends Model[Unit, Unit](buffer, indices)({
  ColoredTexturedVertex setPositionAttribPointer 0
  ColoredTexturedVertex setColorAttribPointer 1
  ColoredTexturedVertex setTextureAttribPointer 2
})({
  GL20 glEnableVertexAttribArray 0
  GL20 glEnableVertexAttribArray 1
  GL20 glEnableVertexAttribArray 2
})

object Model {
  def newVAO: VAOHandle = VAOHandle(GL45 glCreateVertexArrays)

  def newVBO(bufType: Int): VBOHandle = VBOHandle(GL45 glCreateBuffers, bufType)
}
