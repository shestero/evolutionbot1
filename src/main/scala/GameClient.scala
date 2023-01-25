import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.scaladsl.Tcp.OutgoingConnection
import akka.stream.{CompletionStrategy, OverflowStrategy}
import akka.stream.scaladsl.{BidiFlow, Flow, Framing, Sink, Source, Tcp}
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}


class GameClient(bot: Bot)(implicit val system: ActorSystem, val ec: ExecutionContext) {

  val host = "localhost"
  val port = 2323

  val g = new Game(bot)

  val run = Tcp().outgoingConnection(host, port)
    .join(g.flow.watchTermination(){ (_, fut) => fut.onComplete(println) }) // TODO: handle termination of conntection
    .run()

  run.recover {
    case th: Throwable =>
      println("outcomingConnection error: " + th.getMessage)
      Source.failed(th).runWith(Sink.cancelled)
  }.foreach { case c: OutgoingConnection =>
    println(s"New connection $c")
  }
  
}
