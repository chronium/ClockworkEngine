package Material

import org.joml.Vector4f
import rendering.Color
import shaders.ShaderProgramHandle
import textures.{Texture2D, TextureHandle}

trait MaterialType {
  def bind[T](body: => T)(location: String, shader: ShaderProgramHandle): Unit
}

case class FlatColorMaterial(ambient: Color = Color.White, diffuse: Color = Color.White, specular: Color = Color.White) extends MaterialType {
  override def bind[T](body: => T)(location: String, shader: ShaderProgramHandle): Unit = {
    shader setUniform(s"$location.ambient", ambient)
    shader setUniform(s"$location.diffuse", diffuse)
    shader setUniform(s"$location.specular", specular)
    body
  }
}

case class TexturedMaterial(diffuse: TextureHandle = Texture2D.DefaultTexture) extends MaterialType {
  override def bind[T](body: => T)(location: String, shader: ShaderProgramHandle): Unit = {
    shader setUniform("texture_sampler", 0)
    shader setUniform(s"$location.hasTexture", 1)
    diffuse bind(0, body)
  }
}

case class Material(material: MaterialType, reflectance: Float = 1f) {
  def bind[T](body: => T)(shader: ShaderProgramHandle): Unit = {
    shader setUniform("material.reflectance", 1f)
    material.bind({
      body
    })("material", shader)
  }
}

