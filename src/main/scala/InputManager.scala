import scala.collection.mutable

trait Keyboard {
  self =>
  val oldKeys: mutable.HashMap[Int, Boolean] = new mutable.HashMap[Int, Boolean]()
  var keys: mutable.HashMap[Int, Boolean] = new mutable.HashMap[Int, Boolean]()

  val onKeyUpEvents: mutable.MultiMap[Int, Unit => Unit] = new mutable.HashMap[Int, mutable.Set[Unit => Unit]] with mutable.MultiMap[Int, Unit => Unit]
  val onKeyDownEvents: mutable.MultiMap[Int, Unit => Unit] = new mutable.HashMap[Int, mutable.Set[Unit => Unit]] with mutable.MultiMap[Int, Unit => Unit]
  val onKeyPressedEvents: mutable.MultiMap[Int, Unit => Unit] = new mutable.HashMap[Int, mutable.Set[Unit => Unit]] with mutable.MultiMap[Int, Unit => Unit]

  def keyPressed(key: Int): Unit = {
    val old: Boolean = keys getOrElse(key, false)
    keys(key) = true
    oldKeys(key) = old

    if (onKeyDownEvents contains key)
      for (event <- onKeyDownEvents(key))
        event()
  }

  def keyReleased(key: Int): Unit = {
    val old: Boolean = keys getOrElse(key, false)
    keys(key) = false
    oldKeys(key) = old

    if (onKeyUpEvents contains key) {
      for (event <- onKeyUpEvents(key))
        event()
    }

    if (onKeyPressedEvents contains key) {
      if (wasKeyPressed(key))
        for (event <- onKeyPressedEvents(key))
          event()
    }
  }

  def onKeyUp[T](key: Int)(body: => T): Unit = {
    onKeyUpEvents.addBinding(key, _ => body)
  }

  def onKeyDown[T](key: Int)(body: => T): Unit = {
    onKeyDownEvents.addBinding(key, _ => body)
  }

  def onKeyPressed[T](key: Int)(body: => T): Unit = {
    onKeyPressedEvents.addBinding(key, _ => body)
  }

  def isKeyDown(key: Int): Boolean = keys(key)

  def isKeyUp(key: Int): Boolean = !keys(key)

  def wasKeyPressed(key: Int): Boolean = oldKeys(key) && !keys(key)
}

object InputManager extends Keyboard
