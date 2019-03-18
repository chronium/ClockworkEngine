package graph.toml

import toml.Toml
import toml.Codecs._
import OBJLoader.OBJLoader
import graph.BaseEntity
import graph.components.RenderComponent

import scala.io.Source

object ObjectParser {

  case class Root(static_object: Option[StaticObject])

  case class StaticObject(model: String, material: String)

  def :<(path: String): BaseEntity = {
    val source = Source fromFile path

    val tobj = Toml.parseAs[Root](source mkString)

    tobj match {
      case Left((_, ex)) => throw new RuntimeException(ex)
      case Right(value) =>
        if (value.static_object.nonEmpty) {
          val obj = value.static_object.get

          val entity = new BaseEntity
          entity :< new RenderComponent
          entity[RenderComponent].get.model = OBJLoader loadOBJModel ("Assets/" ++ obj.model)
          entity[RenderComponent].get.material = MaterialParser :< ("Assets/" ++ obj.material)

          entity
        } else {
          ???
        }
    }
  }
}
