import Model.{Model, NormalTexturedVertexModel}
import VertexTraits.NormalTexturedVertex

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

object OBJLoader {
  def loadOBJModel(path: String): Model[Unit, Unit] = {
    val source = Source fromFile path

    val verts: ArrayBuffer[Vert] = new ArrayBuffer[Vert]
    val texs: ArrayBuffer[Tex] = new ArrayBuffer[Tex]
    val norms: ArrayBuffer[Norm] = new ArrayBuffer[Norm]

    val faces: ArrayBuffer[Face] = new ArrayBuffer[Face]

    for (line <- source.getLines()) {
      val tokens = line split "\\s+"

      tokens(0) match {
        case "v" => verts += Vert(tokens(1) toFloat, tokens(2) toFloat, tokens(3) toFloat)
        case "vt" => texs += Tex(tokens(1) toFloat, tokens(2) toFloat)
        case "vn" => norms += Norm(tokens(1) toFloat, tokens(2) toFloat, tokens(3) toFloat)
        case "f" => faces += Face(tokens(1), tokens(2), tokens(3))
        case _ =>
      }
    }

    processBuffers(verts, texs, norms, faces)
  }

  def processBuffers(vert: ArrayBuffer[Vert], texts: ArrayBuffer[Tex], norms: ArrayBuffer[Norm], faces: ArrayBuffer[Face]): Model[Unit, Unit] = {
    val indices: ArrayBuffer[Int] = new ArrayBuffer[Int]

    val verts: ArrayBuffer[NormalTexturedVertex] = new ArrayBuffer[NormalTexturedVertex]

    for (pos <- vert) {
      verts += new NormalTexturedVertex setXYZ(pos.x, pos.y, pos.z)
    }

    for (face <- faces) {
      for (ind <- face.indexGroups) {
        val posIndex = ind.pos
        indices += posIndex

        if (ind.tex >= 0) {
          val tex = texts(ind tex)
          verts(posIndex) setST(tex s, tex t)
        }

        if (ind.norm >= 0) {
          val norm = norms(ind norm)
          verts(posIndex) setNXYZ(norm.x, norm.y, norm.z)
        }
      }
    }

    val buffer = NormalTexturedVertex createVertexBuffer (verts: _*)

    new NormalTexturedVertexModel(buffer, indices toArray)
  }

  case class Vert(x: Float, y: Float, z: Float)

  case class Tex(s: Float, t: Float)

  case class Norm(x: Float, y: Float, z: Float)

  case class Face(v1: String, v2: String, v3: String) {
    val indexGroups: Array[IndexGroup] = Array[IndexGroup](parse(v1), parse(v2), parse(v3))

    private def parse(group: String): IndexGroup = {
      val indexGroup = IndexGroup()

      val tokens = group split "/"
      val len = tokens.length
      indexGroup.pos = tokens(0).toInt - 1
      if (len > 1) {
        val texCoord = tokens(1)
        indexGroup.tex = if (texCoord.length > 0) texCoord.toInt - 1 else -1
        if (len > 2) {
          indexGroup.norm = tokens(2).toInt - 1
        }
      }

      indexGroup
    }
  }

  case class IndexGroup(var pos: Int = -1, var tex: Int = -1, var norm: Int = -1)

}
