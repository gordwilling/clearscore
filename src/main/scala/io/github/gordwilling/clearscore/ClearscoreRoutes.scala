package io.github.gordwilling.clearscore

import cats.effect.Concurrent
import cats.implicits._
import io.circe.Encoder
import io.circe.generic.auto._
import io.github.gordwilling.clearscore.CreditCardsService.CreditCardRequest
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{DecodeFailure, HttpRoutes}

object ClearscoreRoutes {

  /**
    * Rounds Doubles to 3 decimals by formatting to a string and parsing the result
    */
  implicit val doubleEncoder: Encoder[Double] = Encoder.encodeDouble.contramap(d => {
    java.lang.Double.parseDouble(f"$d%1.3f")
  })

  /**
    * Defines the <code>/creditCards</code> endpoint
    */
  def creditCardRoutes[F[_] : Concurrent](creditCards: CreditCardsService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case request@POST -> Root / "creditCards" =>
        request.decodeJson[CreditCardRequest].attempt.flatMap {
          case Left(t) => t.getCause match {
            case f: DecodeFailure => BadRequest(f.message)
            case _ => BadRequest(t.toString)
          }
          case Right(creditCardRequest) => for {
            creditCards <- creditCards.eligibleCards(creditCardRequest)
            response <- Ok(creditCards)
          } yield response
        }
    }
  }
}
