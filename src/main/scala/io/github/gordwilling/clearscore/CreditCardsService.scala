package io.github.gordwilling.clearscore

import cats.effect.Concurrent
import cats.implicits._
import io.circe.generic.auto._
import io.github.gordwilling.clearscore.CreditCardScore.cardScore
import io.github.gordwilling.clearscore.CreditCardsService.{CreditCard, CreditCardRequest}
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.client._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io.POST

trait CreditCardsService[F[_]] {
  def eligibleCards(creditCardRequest: CreditCardRequest): F[List[CreditCard]]
}

object CreditCardsService {

  final case class CreditCardRequest(name: String, creditScore: Int, salary: Int)

  final case class CreditCard(provider: String, name: String, apr: Double, cardScore: Double)

  final case class CSCardsRequest(name: String, creditScore: Int)

  final case class CSCardsResponse(cardName: String, apr: Double, eligibility: Double)

  final case class ScoredCardsRequest(name: String, score: Int, salary: Int)

  final case class ScoredCardsResponse(card: String, apr: Double, approvalRating: Double)

  final case class ClientError(e: Throwable) extends RuntimeException

  def apply[F[_]](implicit cc: CreditCardsService[F]): CreditCardsService[F] = cc

  def toCreditCard(c: CSCardsResponse): CreditCard = {
    CreditCard("CSCards", c.cardName, c.apr, cardScore(c.eligibility, c.apr, adjustmentFactor = 10))
  }

  def toCreditCard(c: ScoredCardsResponse): CreditCard = {
    CreditCard("ScoredCards", c.card, c.apr, cardScore(c.approvalRating, c.apr, adjustmentFactor = 100))
  }

  /**
    * Business logic associated with the <code>/creditCards<code> endpoint.
    *
    * @param client         the Http client for making partner requests
    * @param csCardsUri     the URI of the CSCards partner
    * @param scoredCardsUri the URI of the ScoredCards partner
    * @return a List of eligible credit cards for a given request, sorted by card score
    *         in descending order
    */
  def impl[F[_] : Concurrent](client: Client[F], csCardsUri: Uri, scoredCardsUri: Uri): CreditCardsService[F] = new CreditCardsService[F] {
    val dsl: Http4sClientDsl[F] = new Http4sClientDsl[F] {}

    import dsl._

    def eligibleCards(cc: CreditCardRequest): F[List[CreditCard]] = {

      val csCardsRequest = CSCardsRequest(cc.name, cc.creditScore)
      val scoredCardsRequest = ScoredCardsRequest(cc.name, cc.creditScore, cc.salary)

      for {
        csCards <- client.expect[List[CSCardsResponse]](POST(csCardsRequest, csCardsUri))
        scoredCards <- client.expect[List[ScoredCardsResponse]](POST(scoredCardsRequest, scoredCardsUri))
      } yield (csCards.map(toCreditCard) ++ scoredCards.map(toCreditCard))
        .sortBy(_.cardScore)(Ordering[Double].reverse)
    }
  }
}
