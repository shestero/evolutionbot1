import akka.stream.scaladsl.{Flow, Framing, Source}
import akka.util.ByteString

class Game(bot: Bot) {

  val flow = Flow[ByteString]
    .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
    .map(_.utf8String)
    .map(_.trim)
    .via(bot.flow)
    .map(_ + "\n")
    .map(ByteString(_))
  
}
