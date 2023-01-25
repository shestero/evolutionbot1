import scala.util.Random

object Deck {
  enum Suit:
    case Spades, Clubs, Hearts, Diamonds

  enum Rank:
    case `2`, `3`, `4`, `5`, `6`, `7`, `8`, `9`, `10`, Jack, Queen, King, Ace

  given Ordering[Rank] with
    def compare(x: Rank, y: Rank): Int = x.ordinal compare y.ordinal

  case class Card(rank: Rank, suit: Suit)

  def fromStrings(rank: String, suit: String) = Card(Rank.valueOf(rank), Suit.valueOf(suit))

  val full = for {r <- Rank.values; s <- Suit.values} yield Card(r, s)

  protected val rand = new Random(System.currentTimeMillis())
}
