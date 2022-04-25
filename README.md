# Credit Cards Service
*__author: Gordon Wallace__*

This project addresses the ClearScore backend technical test

The solution is implemented in [Scala](https://www.scala-lang.org/) using [Http4s](https://http4s.org/) with [Cats Effect](https://typelevel.org/cats-effect/) and has the following structure:
- __ClearScoreRoutes__
  - Defines the `/creditCards` endpoint
- __ClearScoreServer__
  - Mostly boilerplate containing the Http4s server definition
- __CreditCardsService__
  - Implements the business logic associated with the `/creditCards` endpoint
- __CreditCardsScore__
  - Calculates a card score based on APR and elegibility ratings from partners
- __Main__
  - The server application entry point

## Installation

Running the service requires [Java](https://www.java.com) and [SBT](https://www.scala-sbt.org/) in your path. 
- Download and install Java [here](https://jdk.java.net/java-se-ri/17).
- Download and install SBT using the [instructions](https://www.scala-sbt.org/download.html).

## Running the Service

A `start.sh` script is provided in the root directory of the project for running on linux. This script sets three environment variables that are required to start the server successfully:
- `HTTP_PORT`: The port to expose your service on
- `CSCARDS_ENDPOINT`: The base url for the CSCards partner
- `SCOREDCARDS_ENDPOINT`: The base url for the ScoredCards partner

Modify the `start.sh` script if the default values provided are not sufficient

You may also need to make `start.sh` executable in your shell by running the following command
```
$ chmod u+x start.sh
```
If you have any issues starting up the project, please email me at [gordwilling@yahoo.ca](mailto://gordwilling@yahoo.ca) for assistance!

## Accessing the Service

The server listens on port 8080 by default. You can hit the `/creditCards` endpoint using the following curl command:
```
$ curl -X POST -H "Content-Type: application/json" -d '{"name":"John Smith", "creditScore":500, "salary":28000}' http://localhost:8080/creditCards
```

## Running Tests

run `sbt test` to execute Unit tests
