package graph.components

import Model.Model
import graph.EntityComponent
import shaders.ShaderProgramHandle

class RenderComponent extends EntityComponent {
  var model: Model[Unit, Unit] = _

  def render(shader: ShaderProgramHandle): Unit = {
    if (model != null) {
      entity[TransformComponent].get.setUniform(shader)
      model render()
    }
  }
}
