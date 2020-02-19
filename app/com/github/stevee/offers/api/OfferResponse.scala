package com.github.stevee.offers.api

import java.time.LocalDateTime
import java.util.UUID

import com.github.stevee.offers.domain.Offer
import play.api.libs.json._

case class OfferResponse(id: UUID, price: BigDecimal, currency: String, description: String, start: LocalDateTime, end: LocalDateTime)

object OfferResponse {
  def fromOffer(offer: Offer): OfferResponse = {
    import offer._
    new OfferResponse(id, price, currency, description, start, end)
  }

  implicit val formats: Format[OfferResponse] = Json.format[OfferResponse]
}
