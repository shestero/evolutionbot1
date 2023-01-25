import akka.actor.{ActorRef, ActorSystem, FSM, Props}
import Deck.*

import scala.util.Random

class Bot1(implicit override val system: ActorSystem) extends Bot {

  override def gameNumber: String = "1"

  println(s"Creating bot for game #$gameNumber")

  def decision(card: Card): String = Random.shuffle(List("p", "f")).head

  private case object StateCard extends State

  private final case class DataCard(card: Card) extends Data

  private class BotFSM1 extends BotFSM {

    when(StatePlaying, stateTimeout = timeout) {
      case Event(s"Your card is${_}Card($rank,$suit)${_}", _) =>
        val card = Deck.fromStrings(rank, suit)
        println(s"Bot $name got card $card")
        goto(StateCard).using(DataCard(card))
    }

    when(StateCard, stateTimeout = timeout) {
      case Event("Please choose: p = play or f = fold", DataCard(card)) =>
        val out = decision(card)
        println(s"Note: bot $name make decision: $out")
        outputActor ! out
        stay()
    }

    initialize()
  }

  override val fsm: ActorRef = system.actorOf(Props(classOf[BotFSM1], this))
}
