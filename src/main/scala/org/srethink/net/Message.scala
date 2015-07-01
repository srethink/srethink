package org.srethink.net

case class Message(token: Long, body: String)
case class Handshake(magic: Int, authKey: String, protocol: Int)
