import org.joml.{Matrix4f, Vector3f}
import org.lwjgl.BufferUtils
import org.lwjgl.glfw._
import org.lwjgl.opengl._
import org.lwjgl.glfw.Callbacks._
import org.lwjgl.glfw.GLFW._
import org.lwjgl.opengl.GL11._
import org.lwjgl.system.MemoryStack._
import org.lwjgl.system.MemoryUtil._
import shaders.{FragmentShader, ShaderProgram, ShaderProgramHandle, VertexShader}
import textures.{Texture2D, TextureHandle}

object Main {
  val WIDTH = 800
  val HEIGHT = 600

  var window: Long = _
  var projection: Matrix4f = _

  val inputManager: InputManager = new InputManager

  def main(args: Array[String]): Unit = {
    run()
  }

  def init(): Unit = {
    GLFWErrorCallback createPrint System.err set()

    if (!glfwInit)
      throw new IllegalStateException("Unable to initialize GLFW")

    glfwDefaultWindowHints()
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

    Main.window = glfwCreateWindow(WIDTH, HEIGHT, "Test window", NULL, NULL)
    if (Main.window == NULL) {
      throw new RuntimeException("Failed to create the GLFW window")
    }

    glfwSetKeyCallback(Main.window, (_, key, _, action, _) => {
      action match {
        case GLFW_PRESS => inputManager.keyPressed(key)
        case GLFW_RELEASE => inputManager.keyReleased(key)
        case _ =>
      }
    })

    inputManager.onKeyUp(GLFW_KEY_ESCAPE) {
      glfwSetWindowShouldClose(Main.window, true)
    }

    val stack = stackPush
    val pWidth = stack mallocInt 1
    val pHeight = stack mallocInt 1

    glfwGetWindowSize(Main.window, pWidth, pHeight)

    val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())

    glfwSetWindowPos(Main.window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2)
    stackPop

    glfwMakeContextCurrent(Main.window)

    glfwSwapInterval(1)

    glfwShowWindow(Main.window)

    projection = new Matrix4f perspective(Transform.FOV, pWidth.get(0).toFloat / pHeight.get(0).toFloat, Transform.Z_NEAR, Transform.Z_FAR)
  }

  def loop(): Unit = {
    GL createCapabilities()

    setupTexturedQuad()

    glClearColor(100.0f / 255.0f, 149.0f / 255.0f, 237.0f / 255.0f, 0.0f)

    while (!glfwWindowShouldClose(Main.window)) {
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

      shader bind {
        shader setUniform("tex", 0)
        shader setUniform("projectionMatrix", projection)
        shader setUniform("worldMatrix", transform worldMatrix)
        texture bind model.render
      }

      glfwSwapBuffers(Main.window)
      glfwPollEvents()
    }
  }

  def run(): Unit = {
    init()
    loop()

    glfwFreeCallbacks(Main.window)
    glfwDestroyWindow(Main.window)

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