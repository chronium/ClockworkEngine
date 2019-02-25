class ClockworkEngine(clockwork: Clockwork, title: String, width: Int, height: Int, vSync: Boolean = true, targetFPS: Int = 60, targetUPS: Int = 60) extends Runnable {
  val gameLoopThread = new Thread(this, "CLOCKWORK_LOOP_THREAD")
  val window = new Window(title, width, height, vSync)
  val timer = new Timer

  def start(): Unit = {
    val osName = System getProperty "os.name"
    if (osName contains "Man")
      gameLoopThread run()
    else
      gameLoopThread start()
  }

  override def run(): Unit = {
    init()
    gameLoop()
  }

  def init(): Unit = {
    window init()
    timer init()
    InputManager.window = window.handle
    clockwork init()
  }

  protected def gameLoop(): Unit = {
    var elapsedTime: Double = 0
    var accumulator = 0f
    var interval = 1f / targetFPS

    while (!window.shouldClose) {
      elapsedTime = timer.elapsedTime
      accumulator += elapsedTime.toFloat

      while (accumulator >= interval) {
        update(interval)
        accumulator -= interval
      }

      render()

      if (!window.vSync)
        sync()
    }
  }

  private def sync(): Unit = {
    val loopSlot = 1f / targetFPS
    val endTime = timer.lastTime + loopSlot
    while (timer.time < endTime)
      try Thread.sleep(1)
  }

  def update(deltaTime: Float): Unit = {
    clockwork update deltaTime
  }

  def render(): Unit = {
    clockwork render window
    window update()
  }
}

trait Clockwork {
  def init()

  def update(deltaTime: Float)

  def render(window: Window)
}
