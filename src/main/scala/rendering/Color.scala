package rendering

case class Color(r: Float, g: Float, b: Float, a: Float) {
}

case object Color {
  private implicit def hex2float(hex: String): Float = Integer.parseInt(hex, 16).toFloat

  def RGB(r: Float, g: Float, b: Float): Color = {
    Color(r / 255.0f, g / 255.0f, b / 255.0f, 1.0f)
  }

  def RGBA(r: Float, g: Float, b: Float, a: Float): Color = {
    Color(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f)
  }

  def HEX(value: String): Color = {
    val hex = if (value startsWith "#") value substring 1 else value

    hex.length match {
      case 6 =>
        val r = hex.substring(0, 2)
        val g = hex.substring(2, 4)
        val b = hex.substring(4, 6)

        Color.RGB(r, g, b)
      case 8 =>
        val r = hex.substring(0, 2)
        val g = hex.substring(2, 4)
        val b = hex.substring(4, 6)
        val a = hex.substring(6, 8)

        Color.RGBA(r, g, b, a)
      case _ => throw new RuntimeException(s"Invalid hex color #$hex")
    }
  }

  val White: Color = Color HEX "#FFFFFF"
  val Black: Color = Color HEX "#000000"
  val CornflowerBlue: Color = Color HEX "#6495ED"
}
