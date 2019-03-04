package Material

import textures.{Texture2D, TextureHandle}

case class Material(diffuse: TextureHandle = Texture2D.DefaultTexture) {
  def bind[T](body: => T): Unit = {
    diffuse bind body
  }
}
