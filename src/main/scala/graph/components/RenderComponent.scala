package graph.components

import Material.{Material, FlatColorMaterial}
import Model.Model
import graph.EntityComponent
import shaders.ShaderProgramHandle

class RenderComponent extends EntityComponent {
  var model: Model[Unit, Unit] = _
  var material: Material = Material(new FlatColorMaterial)

  def render(shader: ShaderProgramHandle): Unit = {
    if (model != null) {
      shader.setUniform("worldMatrix", entity.transform.worldMatrix)
      material.bind({
        model render()
      })(shader)
    }
  }
}
