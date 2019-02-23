import org.lwjgl.glfw.GLFW._
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.system.MemoryUtil._

class Window(title: String, width: Int, height: Int, var vSync: Boolean, var handle: Long = NULL) {
  def init(): Unit = {
    GLFWErrorCallback createPrint System.err set()

    if (!glfwInit)
      throw new IllegalStateException("Unable to initialize GLFW")

    glfwDefaultWindowHints()
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

    handle = glfwCreateWindow(width, height, title, NULL, NULL)
    if (handle == NULL)
      throw new RuntimeException("Failed to create GLFW window")

    glfwSetKeyCallback(handle, (_, key, _, action, _) => {
      action match {
        case GLFW_PRESS => InputManager.keyPressed(key)
        case GLFW_RELEASE => InputManager.keyReleased(key)
        case _ =>
      }
    })

    val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor)

    glfwSetWindowPos(handle, (vidmode.width - width) / 2, (vidmode.height - height) / 2)

    glfwMakeContextCurrent(handle)

    if (vSync)
      glfwSwapInterval(1)

    glfwShowWindow(handle)

    GL.createCapabilities(true)
  }

  def shouldClose: Boolean = glfwWindowShouldClose(handle)

  def shouldClose_=(value: Boolean): Unit = glfwSetWindowShouldClose(handle, true)

  def update(): Unit = {
    glfwSwapBuffers(handle)
    glfwPollEvents()
  }
}
