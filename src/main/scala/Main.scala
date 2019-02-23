import Model._
import Transform.Transform
import VertexTraits.{ColoredTexturedVertex, ColoredVertex}
import graph.components.RenderComponent
import graph.{Entity, SceneGraph}
import org.joml.{Matrix4f, Vector3f}
import org.lwjgl.glfw.Callbacks._
import org.lwjgl.glfw.GLFW._
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL11
import shaders.{FragmentShader, ShaderProgram, ShaderProgramHandle, VertexShader}
import textures.{Texture2D, TextureHandle}

object Main extends Clockwork {
  val WIDTH = 800
  val HEIGHT = 600

  var projection: Matrix4f = _
  var clockworkEngine: ClockworkEngine = _

  var sceneGraph: SceneGraph = _

  var wireframe: Boolean = false

  def main(args: Array[String]): Unit = {
    clockworkEngine = new ClockworkEngine(this, "ClockworkEngine", WIDTH, HEIGHT)
    clockworkEngine start()
  }

  override def init(): Unit = {
    InputManager.onKeyUp(GLFW_KEY_ESCAPE) {
      clockworkEngine.window.shouldClose = true
    }

    InputManager.onKeyPressed(GLFW_KEY_F5) {
      wireframe = !wireframe

      if (wireframe)
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
      else
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
    }

    glCullFace(GL_BACK)

    projection = new Matrix4f perspective(Transform.FOV, WIDTH.toFloat / HEIGHT.toFloat, Transform.Z_NEAR, Transform.Z_FAR)
    setupTexturedQuad()

    glClearColor(100.0f / 255.0f, 149.0f / 255.0f, 237.0f / 255.0f, 0.0f)

    sceneGraph = new SceneGraph

    class TestEntity extends Entity {
    }

    val entity = new TestEntity()
    entity :< new RenderComponent
    entity[RenderComponent].get.model = model
    entity.transform = transform

    sceneGraph += entity
  }

  override def render(window: Window): Unit = {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

    shader bind {
      shader setUniform("tex", 0)
      shader setUniform("projectionMatrix", projection)
      texture bind {
        for (renderable <- sceneGraph.get[RenderComponent])
          renderable.render(shader)
      }
    }
  }

  override def update(deltaTime: Float): Unit = {}

  def terminate(): Unit = {
    glfwFreeCallbacks(clockworkEngine.window.handle)
    glfwDestroyWindow(clockworkEngine.window.handle)

    glfwTerminate()
    glfwSetErrorCallback(null).free()
  }

  var model: Model[Unit, Unit] = _
  var shader: ShaderProgramHandle = _
  var texture: TextureHandle = _
  var transform: Transform = new Transform(position = new Vector3f(0, 0, -1))

  def setupColoredQuad(): Unit = {
    val interleavedBuffer = ColoredVertex createVertexBuffer(
      new ColoredVertex setXYZ(-.5f, .5f, 0) setRGBA(1, 0, 0, 1),
      new ColoredVertex setXYZ(-.5f, -.5f, 0) setRGBA(0, 1, 0, 1),
      new ColoredVertex setXYZ(.5f, -.5f, 0) setRGBA(0, 0, 1, 1),
      new ColoredVertex setXYZ(.5f, .5f, 0) setRGBA(1, 1, 1, 1))

    val indices = Array[Short](
      0, 1, 2,
      2, 3, 0)

    model = new VertexColorModel(interleavedBuffer, indices)

    val vert = VertexShader |:| "Assets/Shaders/passthrough.vs"
    val frag = FragmentShader |:| "Assets/Shaders/passthrough.fs"

    shader = ShaderProgram(vert, frag) bindAttribLocations((0, "vPos"), (1, "vColor")) build
  }

  def setupTexturedQuad(): Unit = {
    val buffer = ColoredTexturedVertex createVertexBuffer(
      new ColoredTexturedVertex setXYZ(-.5f, .5f, 0f) setRGBA(1, 0, 0, 1) setST(0, 1),
      new ColoredTexturedVertex setXYZ(-.5f, -.5f, 0f) setRGBA(0, 1, 0, 1) setST(0, 0),
      new ColoredTexturedVertex setXYZ(.5f, -.5f, 0f) setRGBA(0, 0, 1, 1) setST(1, 0),
      new ColoredTexturedVertex setXYZ(.5f, .5f, 0f) setRGBA(1, 1, 1, 1) setST(1, 1))

    val indices = Array[Short](
      0, 1, 2,
      2, 3, 0)

    model = new VertexColorTextureModel(buffer, indices)

    texture = Texture2D createTexture2D "Assets/Textures/WoodFloor22_col.jpg"

    val vert = VertexShader |:| "Assets/Shaders/vsTextured.glsl"
    val frag = FragmentShader |:| "Assets/Shaders/fsTextured.glsl"

    shader = ShaderProgram(vert, frag) bindAttribLocations((0, "vPos"), (1, "vColor")) build
  }
}