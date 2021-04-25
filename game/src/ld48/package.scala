import indigo.shared.time.Seconds
package object ld48 {
  implicit def autoSome[A](a: A): Option[A] = Some(a)
  implicit def timeToDouble(t: Seconds)     = t.toDouble
}
