package VertexTraits

import java.nio.FloatBuffer

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.{GL11, GL20}

trait VertexPositionAttribute {
  self =>
  private val xyzw = Array[Float](0, 0, 0, 1)

  def setXYZ(x: Float, y: Float, z: Float): self.type = setXYZW(x, y, z, 1)

  def setXYZW(x: Float, y: Float, z: Float, w: Float): self.type = {
    xyzw(0) = x
    xyzw(1) = y
    xyzw(2) = z
    xyzw(3) = w
    this
  }

  def putXYZW(out: FloatBuffer): FloatBuffer = out.put(xyzw)

  def getXYZW: Array[Float] = xyzw.clone
}

trait VertexColorAttribute {
  self =>
  private val rgba = Array[Float](1, 1, 1, 1)

  def setRGB(r: Float, g: Float, b: Float): self.type = setRGBA(r, g, b, 1)

  def setRGBA(r: Float, g: Float, b: Float, a: Float): self.type = {
    rgba(0) = r
    rgba(1) = g
    rgba(2) = b
    rgba(3) = a
    this
  }

  def putRGBA(out: FloatBuffer): FloatBuffer = out.put(rgba)

  def getRGBA: Array[Float] = rgba.clone
}

trait VertexTextureAttribute {
  self =>
  private val st = Array[Float](0, 0)

  def setST(s: Float, t: Float): self.type = {
    st(0) = s
    st(1) = t
    this
  }

  def putST(out: FloatBuffer): FloatBuffer = out.put(st)

  def getST: Array[Float] = st.clone
}

trait VertexMeta[V1] extends VertexMetaBase {
  type V = V1
}

trait VertexMetaBase {
  type V

  private var _numElements = 0
  private var currentByteOffset = 0
  private var putFunction: (V, FloatBuffer) => Unit = { (_, _) => () }

  protected def registerAttribute(numElements: Int, put: (V, FloatBuffer) => Unit): VertexAttributeInformation[V] = {
    this._numElements += numElements
    val byteCount = numElements * 4
    val info = VertexAttributeInformation(numElements, currentByteOffset, put)
    val currentPutter = putFunction
    putFunction = { (v, b) => currentPutter(v, b); put(v, b) }
    currentByteOffset += byteCount
    info
  }

  def stride: Int = currentByteOffset

  def numElements: Int = _numElements

  def createVertexBuffer(verts: V*): FloatBuffer = {
    val buffer = BufferUtils.createFloatBuffer(verts.size * numElements)
    for (v <- verts) putFunction(v, buffer)
    buffer.flip
    buffer
  }
}

case class VertexAttributeInformation[V](numElements: Int, byteOffset: Int, put: (V, FloatBuffer) => Unit)

trait VertexPositionMeta {
  self: VertexMetaBase {type V <: VertexPositionAttribute} =>
  private val positionInfo = registerAttribute(4, { (v, b) => v.putXYZW(b) })

  def setPositionAttribPointer(index: Int): Unit =
    GL20 glVertexAttribPointer(index, positionInfo numElements, GL11 GL_FLOAT, false, stride, positionInfo byteOffset)
}

trait VertexColorMeta {
  self: VertexMetaBase {type V <: VertexColorAttribute} =>
  private val colorInfo = registerAttribute(4, { (v, b) => v.putRGBA(b) })

  def setColorAttribPointer(index: Int): Unit =
    GL20 glVertexAttribPointer(index, colorInfo numElements, GL11 GL_FLOAT, false, stride, colorInfo byteOffset)
}

trait VertexTextureMeta {
  self: VertexMetaBase {type V <: VertexTextureAttribute} =>
  private val textureInfo = registerAttribute(2, { (v, b) => v.putST(b) })

  def setTextureAttribPointer(index: Int): Unit =
    GL20 glVertexAttribPointer(index, textureInfo numElements, GL11 GL_FLOAT, false, stride, textureInfo byteOffset)
}

class ColoredTexturedVertex
  extends VertexPositionAttribute
    with VertexColorAttribute
    with VertexTextureAttribute

object ColoredTexturedVertex extends VertexMeta[ColoredTexturedVertex]
  with VertexPositionMeta
  with VertexColorMeta
  with VertexTextureMeta

class ColoredVertex
  extends VertexPositionAttribute
    with VertexColorAttribute

object ColoredVertex extends VertexMeta[ColoredVertex]
  with VertexPositionMeta
  with VertexColorMeta

