package Material

import shaders.ShaderProgramHandle
import textures.{Texture2D, TextureHandle}

case class Material(diffuse: TextureHandle = Texture2D.DefaultTexture) {
  def bind[T](body: => T)(shader: ShaderProgramHandle): Unit = {
    shader setUniform("texture_sampler", 0)
    diffuse bind body
  }
}
