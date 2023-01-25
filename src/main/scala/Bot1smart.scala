import akka.actor.ActorSystem

import scala.math.Ordered.orderingToOrdered

class Bot1smart(implicit override val system: ActorSystem) extends Bot1 {
  override def name: String = super.name ++ "-smart"
  override def decision(card: Deck.Card): String = if (card.rank > Deck.Rank.`8`) "p" else "f"
}
