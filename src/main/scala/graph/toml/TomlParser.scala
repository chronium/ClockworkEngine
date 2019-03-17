package graph.toml

import toml.{Codec, Toml, Value}
import toml.Codecs._

object TomlParser {
  val data: String =
    """
      |[renderer]
      |  ambient = [0.15, 0.15, 0.15]
      |  spec_power = 10.0
      |  clear_color = "6495ED"
    """.stripMargin

  case class Root(renderer: Renderer)

  case class Renderer(ambient: Color, spec_power: Float, clear_color: Color)

  case class Color(color: Either[String, List[Float]])

  implicit val floatCodec: Codec[Float] = Codec {
    case (Value.Real(v), _, _) => Right(v.toFloat)
    case (value, _, _) => Left((List(), s"Expected float, $value provided"))
  }

  implicit val colorCodec: Codec[Color] = Codec {
    case (Value.Str(value), _, _) =>
      Right(Color(Left(value)))
    case (Value.Arr(List(Value.Real(r), Value.Real(g), Value.Real(b))), _, _) => Right(Color(Right(List[Float](r toFloat, g toFloat, b toFloat))))
    case (value, _, _) => Left((List(), s"Expected hex or rgb array, $value provided"))
  }

  val toml_data = Toml.parseAs[Root](data)
}
