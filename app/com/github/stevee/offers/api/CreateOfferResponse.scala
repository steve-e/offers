package com.github.stevee.offers.api

case class CreateOfferResponse(id: String)

object CreateOfferResponse {

  import play.api.libs.json._

  implicit val formats = Json.format[CreateOfferResponse]
}