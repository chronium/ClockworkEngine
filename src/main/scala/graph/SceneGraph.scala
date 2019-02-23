package graph

class SceneGraph {
  val root: Entity = new SceneEntity

  def +(other: Entity): this.type = {
    root.children += other
    this
  }
}

class SceneEntity extends Entity {
}
