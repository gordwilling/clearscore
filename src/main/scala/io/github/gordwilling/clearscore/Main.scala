package io.github.gordwilling.clearscore

import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.Port
import org.http4s.Uri

object Main extends IOApp {

  case class Env(httpPort: Port, csCardsEndpoint: Uri, scoredCardsEndpoint: Uri);

  def printUsage(): Unit = {
    System.out.println(
      s"""
         |Set the following environment variables prior to execution:
         |
         |  HTTP_PORT: The port to expose your service on
         |  CSCARDS_ENDPOINT: The base url for CSCards
         |  SCOREDCARDS_ENDPOINT: The base url for ScoredCards
         |""".stripMargin
    )
  }

  /**
    * Server Application entry point.
    */
  def run(args: List[String]): IO[ExitCode] = {
    val env = for {
      parsedHttpPort <- Option(System.getenv("HTTP_PORT")).map(Port.fromString)
      httpPort <- parsedHttpPort
      parsedCsCardsEndpoint <- Option(System.getenv("CSCARDS_ENDPOINT")).map(Uri.fromString)
      csCardsEndpoint <- parsedCsCardsEndpoint.toOption
      parsedScoredCardsEndpoint <- Option(System.getenv("SCOREDCARDS_ENDPOINT")).map(Uri.fromString)
      scoredCardsEndpoint <- parsedScoredCardsEndpoint.toOption
    } yield Env(httpPort, csCardsEndpoint, scoredCardsEndpoint)

    env match {
      case Some(e) =>
        ClearscoreServer
          .stream[IO](e.httpPort, e.csCardsEndpoint, e.scoredCardsEndpoint)
          .compile.drain.as(ExitCode.Success)
      case _ =>
        printUsage()
        IO(ExitCode.Error)
    }
  }
}
