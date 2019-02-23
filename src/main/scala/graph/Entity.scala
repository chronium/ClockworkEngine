package graph

import scala.collection.mutable.ArrayBuffer

trait Entity {
  private var _children: ArrayBuffer[Entity] = new ArrayBuffer[Entity]
  private var _components: ArrayBuffer[EntityComponent] = new ArrayBuffer[EntityComponent]
  var parent: Entity = _

  def children: ArrayBuffer[Entity] = _children

  def children_=(children: ArrayBuffer[Entity]): Unit = _children = children

  def components: ArrayBuffer[EntityComponent] = _components

  def components_=(components: ArrayBuffer[EntityComponent]): Unit = _components = components

  def apply[T <: EntityComponent]: Option[T] = {
    for (comp <- components) {
      comp match {
        case t: T => return Some(t)
        case _ =>
      }
    }
    None
  }
}
