import concurrent.ExecutionContext.Implicits.global
import com.typesafe.config.ConfigFactory
import akka.actor.{ActorRef, ActorSystem, Props}

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn
import scala.util.{Success, Try}

object Main {
  val app = "evolutiobot1"
  val ver = "23 Jan, 23"

  println(s"$app\tVersion $ver by Michael Shestero")

  val config = ConfigFactory.parseString("akka.loglevel = WARNING")
  implicit val system: ActorSystem = ActorSystem(app, config)
  println("ActorSystem is initialized")

  @main def main(args: String*): Unit = {
    val bot: Bot = Try { args(0) }.map(_.trim.toLowerCase) match
      case Success("smart") => new Bot1smart
      case _ => new Bot1

    new GameClient(bot)

    println("Press RETURN to stop...")
    StdIn.readLine() // suspend the Main until a presses Enter
    system.terminate().foreach { t => println(s"$app\tterminated; " + t) }
  }

  println(s"$app\tBYE")
}
