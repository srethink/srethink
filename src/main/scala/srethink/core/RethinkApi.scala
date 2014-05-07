package srethink.core

/**
 * type R : api return type
 */
trait Api[R] {
}

object Api {

  object Durablity {
    val Hard = "hard"
    val Soft = "sort"
  }
}
