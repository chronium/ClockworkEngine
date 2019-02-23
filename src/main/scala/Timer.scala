class Timer {
  private[this] var _lastTime: Double = _

  def init(): Unit = lastTime = time

  def time: Double = System.nanoTime / 1000000000.0

  def elapsedTime: Double = {
    val _time = time
    val elapsedTime = (_time - lastTime).toFloat
    lastTime = _time
    elapsedTime
  }

  def lastTime: Double = _lastTime

  def lastTime_=(value: Double): Unit = _lastTime = value
}
