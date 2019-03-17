package graph.toml

import Material.{Material, FlatColorMaterial}
import rendering.Color
import toml.{Codec, Toml, Value}
import toml.Codecs._

import scala.io.Source

object MaterialParser {

  case class Root(flat_color: Option[FlatColor])

  case class FlatColor(ambient: Color, diffuse: Color, specular: Color)

  implicit val colorCodec: Codec[Color] = Codec {
    case (Value.Arr(List(Value.Real(r), Value.Real(g), Value.Real(b))), _, _) => Right(Color(r toFloat, g toFloat, b toFloat, 1))
    case (Value.Arr(List(Value.Real(r), Value.Real(g), Value.Real(b), Value.Real(a))), _, _) => Right(Color(r toFloat, g toFloat, b toFloat, a toFloat))
    case (Value.Str(value), _, _) => if (!Seq(6, 7, 8, 9).contains(value.length)) Left((List(), s"Hex color incorrect, $value provided")) else Right(Color.HEX(value))
    case (value, _, _) => Left((List(), s"Expected hex or rgb color array, $value provided"))
  }

  def :<(path: String): Material = {
    val source = Source fromFile path

    val mat = Toml.parseAs[Root](source mkString)

    mat match {
      case Left((_, ex)) => throw new RuntimeException(ex)
      case Right(material) =>
        if (material.flat_color.nonEmpty) {
          val mat = material.flat_color.get
          Material(FlatColorMaterial(mat.ambient, mat.diffuse, mat.specular))
        } else {
          ???
        }
    }
  }
}
