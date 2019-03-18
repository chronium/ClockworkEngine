package graph.toml

import graph.SceneGraph
import org.joml.Vector3f
import rendering.Color
import toml.{Codec, Toml, Value}
import MaterialParser.colorCodec
import Transform.Transform
import toml.Codecs._

import scala.collection.mutable
import scala.io.Source

object SceneParser {

  case class Root(renderer: Renderer, world: World)

  case class Renderer(ambient: Color, spec_power: Float, clear_color: Color)

  case class World(objects: List[Object], transforms: List[WorldTransform], entities: List[Entity])

  case class WorldTransform(name: String, position: Vector3f)

  case class Object(name: String, obj: String)

  case class Entity(wobj: String, transform: String)

  implicit val floatCodec: Codec[Float] = Codec {
    case (Value.Real(v), _, _) => Right(v.toFloat)
    case (value, _, _) => Left((List(), s"Expected float, $value provided"))
  }

  implicit val vector3Codec: Codec[Vector3f] = Codec {
    case (Value.Arr(List(Value.Real(x), Value.Real(y), Value.Real(z))), _, _) => Right(new Vector3f(x toFloat, y toFloat, z toFloat))
    case (value, _, _) => Left((List(), s"Expected vector3, $value provided"))
  }

  def :<(path: String): SceneGraph = {
    val source = Source fromFile path

    val toml = Toml.parseAs[Root](source mkString)

    toml match {
      case Left((_, ex)) => throw new RuntimeException(ex)
      case Right(scene) =>
        var graph = new SceneGraph

        graph.ambientColor = scene.renderer.ambient
        graph.clearColor = scene.renderer.clear_color
        graph.specular_power = scene.renderer.spec_power

        val world_objects = mutable.Map[String, String]()
        val world_transforms = mutable.Map[String, Transform]()

        for (obj <- scene.world.objects)
          world_objects(obj.name) = obj.obj

        for (trans <- scene.world.transforms)
          world_transforms(trans.name) = new Transform(trans.position)

        for (entity <- scene.world.entities) {
          val world_entity = ObjectParser :< "Assets/" ++ world_objects(entity.wobj)
          world_entity.transform = world_transforms(entity.transform)
          graph += world_entity
        }

        graph
    }
  }
}
