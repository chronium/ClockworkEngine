package graph

trait EntityComponent {
  private[this] var _entity: Entity = _

  def entity: Entity = _entity

  def entity_=(entity: Entity): Unit = _entity = entity
}
