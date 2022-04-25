package io.github.gordwilling.clearscore

import org.scalacheck.Gen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class CreditCardScoreSpec extends AnyFunSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  implicit val genEligibility: Gen[Double] = Gen.choose(0.1, 10.0)
  implicit val genApr: Gen[Double] = Gen.choose(0.1, 100.0)
  implicit val genAdjustmentFacctor: Gen[Int] = Gen.oneOf(10, 100)

  describe("cardScore") {

    it("given apr = 21.4, eligibility = 6.3, adjustmentFactor = 10, gives cardScore 0.137") {
      val cardScore = CreditCardScore.cardScore(6.3, 21.4, 10)
      cardScore shouldEqual 0.137 +- 0.001
    }

    it("calculates card score based on any reasonable adjusted card score and apr") {

      /**
        * Inverse function to recover eligibility from a Card Score
        */
      def eligibilityFromCardScore(cardScore: Double, apr: Double, adjustmentFactor: Int) = {
        cardScore / adjustmentFactor / Math.pow(1 / apr, 2)
      }

      /**
        * Inverse function to recover apr from a Card Score
        */
      def aprFromCardScore(cardScore: Double, eligibility: Double, adjustmentFactor: Int) = {
        1 / Math.sqrt(cardScore / eligibility / adjustmentFactor)
      }

      forAll(genEligibility, genApr, genAdjustmentFacctor) {
        (eligibility: Double, apr: Double, adjustmentFactor: Int) => {

            val cardScore = CreditCardScore.cardScore(eligibility, apr, adjustmentFactor)
            val recoveredEligibility = eligibilityFromCardScore(cardScore, apr, adjustmentFactor)
            val recoveredApr = aprFromCardScore(cardScore, eligibility, adjustmentFactor)

            recoveredEligibility shouldEqual eligibility +- 0.00000001
            recoveredApr shouldEqual apr +- 0.00000001
          }
      }
    }
  }
}
