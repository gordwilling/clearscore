package io.github.gordwilling.clearscore

import cats.effect.{Async, Resource}
import cats.implicits.catsSyntaxFlatMapOps
import com.comcast.ip4s.{IpLiteralSyntax, Port}
import fs2.Stream
import org.http4s.Uri
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object ClearscoreServer {

  /**
    * Defines the Http4s server
    * @param httpPort the port to listen on
    * @param csCardsUri the URI of the CSCards partner
    * @param scoredCardsUri the URI of the ScoredCards partner
    */
  def stream[F[_] : Async](httpPort: Port, csCardsUri: Uri, scoredCardsUri: Uri): Stream[F, Nothing] = {
    for {
      client <- Stream.resource(EmberClientBuilder.default[F].build)
      creditCards = CreditCardsService.impl[F](client, csCardsUri, scoredCardsUri)

      httpApp = (
        ClearscoreRoutes.creditCardRoutes[F](creditCards)
        ).orNotFound

      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

      exitCode <- Stream.resource(
        EmberServerBuilder.default[F]
          .withHost(ipv4"127.0.0.1")
          .withPort(httpPort)
          .withHttpApp(finalHttpApp)
          .build >>
          Resource.eval(Async[F].never)
      )
    } yield exitCode
  }.drain
}
