package actors

import akka.actor._

object MyWebSocketActor {
  def props(out: ActorRef): Props = Props(new MyWebSocketActor(out))
}

class MyWebSocketActor(out: ActorRef) extends Actor {
  def receive: Receive = {
    case msg: String =>
      out ! ("I received your message: " + msg)
  }
}
