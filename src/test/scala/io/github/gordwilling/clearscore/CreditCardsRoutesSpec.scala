package io.github.gordwilling.clearscore

import cats.effect.IO
import io.circe.generic.encoding.DerivedAsObjectEncoder.deriveEncoder
import io.github.gordwilling.clearscore.CreditCardsService.{CreditCard, CreditCardRequest}
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.implicits._

class CreditCardsRoutesSpec extends CatsEffectSuite {

  private val creditCardsProducesBadRequest: IO[Response[IO]] = {
    val postToCreditCards = Request[IO](Method.POST, uri = uri"/creditCards")
      .withEntity("""{"Malformed Json": "Makes for a Bad Request"}""")

    val creditCards = new CreditCardsService[IO] {
      override def eligibleCards(creditCardRequest: CreditCardRequest): IO[List[CreditCard]] = {
        throw new IllegalStateException("Should not get this far")
      }
    }
    ClearscoreRoutes.creditCardRoutes(creditCards).orNotFound(postToCreditCards)
  }

  private val creditCardsProducesSuccess: IO[Response[IO]] = {
    val postToCreditCards = Request[IO](Method.POST, uri = uri"/creditCards")
      .withEntity(CreditCardRequest("Sample Name", 700, 200000))

    val creditCards = new CreditCardsService[IO] {
      override def eligibleCards(creditCardRequest: CreditCardRequest): IO[List[CreditCard]] = {
        IO.pure(List(
          CreditCard("Sample Provider 1", "Sample Name 1", 20.1, 0.2),
          CreditCard("Sample Provider 1", "Sample Name 2", 19.1, 0.3),
          CreditCard("Sample Provider 2", "Sample Name 3", 29.1, 0.4),
        ))
      }
    }
    ClearscoreRoutes.creditCardRoutes(creditCards).orNotFound(postToCreditCards)
  }

  test("POST /creditCards returns status code 400 when the Json body is malformed") {
    assertIO(creditCardsProducesBadRequest.map(_.status), Status.BadRequest)
  }

  test("POST /creditCards returns status code 200 when the Json body is correct") {
    assertIO(creditCardsProducesSuccess.map(_.status), Status.Ok)
  }
}