import Camera.Camera
import Model._
import Transform.Transform
import VertexTraits.{ColoredTexturedVertex, ColoredVertex}
import graph.components.RenderComponent
import graph.toml.{MaterialParser, ObjectParser, SceneParser}
import graph.{BaseEntity, Entity, SceneGraph}
import org.joml.{Matrix4f, Vector2f, Vector3f, Vector4f}
import org.lwjgl.glfw.Callbacks._
import org.lwjgl.glfw.GLFW._
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL11
import rendering.{Attenuation, Color, PointLight}
import shaders.{FragmentShader, ShaderProgram, ShaderProgramHandle, VertexShader}
import textures.{Texture2D, TextureHandle}

object Main extends Clockwork {
  val WIDTH = 800
  val HEIGHT = 600

  var projection: Matrix4f = _
  var clockworkEngine: ClockworkEngine = _

  var sceneGraph: SceneGraph = _

  var wireframe: Boolean = false
  var lockMouse: Boolean = false

  var camera: Camera = new Camera

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

    InputManager.onKeyPressed(GLFW_KEY_F6) {
      lockMouse = !lockMouse

      if (lockMouse) {
        glfwSetCursorPos(clockworkEngine.window.handle, (WIDTH / 2).toDouble, (HEIGHT / 2).toDouble)
        glfwSetInputMode(clockworkEngine.window.handle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN)
      }
      else {
        glfwSetInputMode(clockworkEngine.window.handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
      }
    }

    glCullFace(GL_BACK)
    glEnable(GL_CULL_FACE)

    glEnable(GL_DEPTH_TEST)

    projection = new Matrix4f perspective(Transform.FOV, WIDTH.toFloat / HEIGHT.toFloat, Transform.Z_NEAR, Transform.Z_FAR)
    setupTexturedQuad()

    sceneGraph = SceneParser :< "Assets/Scenes/scene.toml"

    glClearColor(sceneGraph.clearColor r, sceneGraph.clearColor g, sceneGraph.clearColor b, 0.0f)
  }

  override def render(window: Window): Unit = {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

    val curLight = light.intoView(camera.transform.cameraMatrix)

    shader bind {
      shader setUniform("projectionMatrix", projection)
      shader setUniform("viewMatrix", camera.transform.cameraMatrix)
      shader setUniform("pointLight", curLight)
      shader setUniform("ambientLight", sceneGraph.ambientColor)
      shader setUniform("specularPower", sceneGraph.specular_power)
      for (renderable <- sceneGraph.get[RenderComponent])
        renderable.render(shader)
    }
  }

  var mouseSpeed: Float = 15f

  override def update(deltaTime: Float): Unit = {
    val move = new Vector3f(0)

    if (InputManager.isKeyDown(GLFW_KEY_W))
      move.z += -1 * deltaTime
    if (InputManager.isKeyDown(GLFW_KEY_S))
      move.z += 1 * deltaTime

    if (InputManager.isKeyDown(GLFW_KEY_A))
      move.x += -1 * deltaTime
    if (InputManager.isKeyDown(GLFW_KEY_D))
      move.x += 1 * deltaTime

    if (InputManager.isKeyDown(GLFW_KEY_SPACE))
      move.y += 1 * deltaTime
    if (InputManager.isKeyDown(GLFW_KEY_LEFT_SHIFT))
      move.y += -1 * deltaTime

    camera.move(move)

    if (lockMouse) {
      val deltaY = InputManager.mousePosition.y - 300
      val deltaX = InputManager.mousePosition.x - 400

      camera.transform.rotation.x += deltaY * deltaTime * mouseSpeed
      camera.transform.rotation.y += deltaX * deltaTime * mouseSpeed

      if (camera.transform.rotation.x >= 90f)
        camera.transform.rotation.x = 90f
      if (camera.transform.rotation.x <= -90f)
        camera.transform.rotation.x = -90f

      if (camera.transform.rotation.y >= 360f)
        camera.transform.rotation.y = 0
      if (camera.transform.rotation.y <= -360f)
        camera.transform.rotation.y = 0

      glfwSetCursorPos(clockworkEngine.window.handle, (WIDTH / 2).toDouble, (HEIGHT / 2).toDouble)
    }
  }

  def terminate(): Unit = {
    glfwFreeCallbacks(clockworkEngine.window.handle)
    glfwDestroyWindow(clockworkEngine.window.handle)

    glfwTerminate()
    glfwSetErrorCallback(null).free()
  }

  var shader: ShaderProgramHandle = _
  var transform: Transform = new Transform(position = new Vector3f(0, 0, -2))

  var light: PointLight = _

  def setupTexturedQuad(): Unit = {
    val vert = VertexShader |:| "Assets/Shaders/vsLit.glsl"
    val frag = FragmentShader |:| "Assets/Shaders/fsLit.glsl"

    shader = ShaderProgram(vert, frag) bindAttribLocations((0, "vPos"), (1, "vTexCoord"), (2, "vNormal")) build

    val atten = new Attenuation(0, 0, 1)
    light = new PointLight(new Vector3f(1), new Vector3f(0, 0, 1), 2.0f, atten)
  }
}