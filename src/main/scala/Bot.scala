import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorRef, ActorSystem, FSM, Props}
import akka.stream.{CompletionStrategy, DelayOverflowStrategy, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration.*
import scala.language.postfixOps

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

object Bot {
  protected def nameGenerator: String = {
    val fmt = "yyMMdd_hhmmss"
    val formatter = DateTimeFormatter.ofPattern(fmt).withZone(ZoneId.systemDefault())
    val tstamp = formatter.format(Instant.now())
    "Bot-" + tstamp
  }
}

trait Bot(private val login: String = Bot.nameGenerator, password: String = "1")(implicit val system: ActorSystem) {

  val timeout = 10 minutes // 20 second

  def name: String = login

  def gameNumber: String

  protected trait State

  protected case object StateStart extends State

  protected case object StatePlaying extends State

  protected trait Data

  protected case object Uninitialized extends Data

  protected class BotFSM extends FSM[State, Data] {

    var counter: Int = 0
    def inc: Int = { counter = counter+1; counter }

    when(StateStart, stateTimeout = timeout) {
      // case Event(s"$gameNumber\tSingle-card game", d) => // Scala Bug?
      case Event(s"${gameNumber}Single-card game", d) =>
        outputActor ! gameNumber.trim
        println(s"$name going to play game #$inc")
        goto(StatePlaying).using(d)
    }

    whenUnhandled {
      case Event(StateTimeout, _) =>
        println("WARNING: time-out!")
        outputActor ! Done
        stop()

      case Event(s"${_}game over!", _) =>
        println(s"$name got game over!")
        goto(StateStart).using(Uninitialized) // repeat forever

      case Event(s"The cards are dealt. Round #$round", _) =>
        println(s"Note: round#$round")
        stay()

      case Event(s"You are playing in $gameName game with $partners", _) =>
        println(s"Note: game #$counter partners are: $partners")
        stay()

      case Event(s"You, $name have $tokens tokens.", _) =>
        println(s"Note: bot $name has $tokens token(s)")
        stay()

      case Event(s"You have $tokens token(s) now", _) =>
        println(s"Note: bot $name has $tokens token(s) playing game #$counter")
        stay()

      case Event(s, _) => // suppress Akka warnings
        //println(s"Debug [$stateData]: $s")
        stay()

    }

    startWith(StateStart, Uninitialized, Some(timeout))
  }

  protected val fsm: ActorRef // = system.actorOf(Props(classOf[BotFSM], this))

  def talkingSpeed = 1 second

  protected val (outputActor, botOutput) = Source
    .actorRef[String](
      completionMatcher = {
        case Done =>
          println("catched done")
          CompletionStrategy.immediately // complete stream immediately if we send it Done
      },
      failureMatcher = PartialFunction.empty, // never fail the stream because of a message
      bufferSize = 64,
      overflowStrategy = OverflowStrategy.dropHead
    )
    .preMaterialize()

  private val botInput: Sink[String, Future[Done]] = Sink.foreach[String](fsm ! _)

  val enter = Source[String](Seq(login, password))

  val output = (enter ++ botOutput).delay(talkingSpeed, DelayOverflowStrategy.emitEarly) // .throttle(1, talkingSpeed)

  val flow: Flow[String, String, _] = Flow.fromSinkAndSourceCoupled(botInput, output)
}
