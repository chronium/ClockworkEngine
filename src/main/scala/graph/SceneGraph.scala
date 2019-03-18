package graph

import rendering.Color

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

class SceneGraph {
  val root: Entity = new SceneEntity

  var ambientColor: Color = Color(0.15f, 0.15f, 0.15f)
  var clearColor: Color = Color CornflowerBlue

  var specular_power: Float = 10.0f

  def +(other: Entity): this.type = {
    other.parent = root
    root.children += other
    this
  }

  def get[T <: EntityComponent : ClassTag]: ArrayBuffer[T] = {
    var entities = new ArrayBuffer[T]

    for (entity <- root.children)
      entity[T] match {
        case Some(comp) => entities += comp
        case None =>
      }

    entities
  }
}

class SceneEntity extends Entity {
}
