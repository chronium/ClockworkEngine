package graph

import Transform.Transform

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

trait Entity {
  private[this] var _children: ArrayBuffer[Entity] = new ArrayBuffer[Entity]
  private[this] var _components: ArrayBuffer[EntityComponent] = new ArrayBuffer[EntityComponent]
  var parent: Entity = _

  private[this] var _transform: Transform = new Transform()

  def transform: Transform = _transform

  def transform_=(transform: Transform): Unit = _transform = transform

  def children: ArrayBuffer[Entity] = _children

  def children_=(children: ArrayBuffer[Entity]): Unit = _children = children

  def components: ArrayBuffer[EntityComponent] = _components

  def components_=(components: ArrayBuffer[EntityComponent]): Unit = _components = components

  def :<(component: EntityComponent): Unit = {
    component.entity = this
    components += component
  }

  def apply[T <: EntityComponent : ClassTag](): Option[T] = {
    for (comp <- components) {
      comp match {
        case t: T => return Some(t)
        case _ =>
      }
    }
    None
  }
}

class BaseEntity extends Entity
