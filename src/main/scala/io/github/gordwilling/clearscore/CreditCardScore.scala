package io.github.gordwilling.clearscore

object CreditCardScore {

  /**
    * Calculates a card score based on eligibility, apr and an adjustmentFactor, where
    * the adjustmentFactor is used to compensate for different eligibility scales coming
    * from partners
    */
  def cardScore(eligibility: Double, apr: Double, adjustmentFactor: Int): Double = {
    eligibility * adjustmentFactor * Math.pow(1 / apr, 2)
  }
}
