package org.srethink

package object core {
  type Tagged[U] = { type Tag = U }
  type @@[T, U] = T with Tagged[U]
}
