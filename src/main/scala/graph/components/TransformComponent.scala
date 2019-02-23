package graph.components

import Transform.Transform
import graph.EntityComponent
import shaders.ShaderProgramHandle

class TransformComponent extends EntityComponent {
  var transform: Transform = Transform.Identity

  def setUniform(shader: ShaderProgramHandle): Unit = {
    shader setUniform("worldMatrix", transform worldMatrix)
  }
}
