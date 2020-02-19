package com.github.stevee.offers.api

import java.time.{LocalDate, LocalDateTime}

import org.specs2.mutable.Specification
import java.util.UUID

import org.specs2.mutable.Specification
import play.api.libs.json.Json

class OfferResponseSpec extends Specification {

  val id: UUID = UUID.randomUUID()
  val price = BigDecimal("5.99")
  val currency = "EUR"
  val description = "Westmalle Dubbel"
  val startDateTime = LocalDate.of(2020, 2, 20).atStartOfDay()
  val endDateTime: LocalDateTime = startDateTime.plusMonths(1L)

  val responseBody =
    s"""{
       |"id" : "$id",
       | "price" : $price,
       | "currency" : "$currency",
       | "description" : "$description",
       | "start" : "2020-02-20T00:00:00",
       | "end" : "2020-03-20T00:00:00"
       | }""".stripMargin

  "CreateOfferRequest" should {

    "be rendered as json string" in {
      val serialized = Json.toJson(OfferResponse(id, price, currency, description, startDateTime, endDateTime))
      val parsed = Json.parse(responseBody)
      serialized mustEqual parsed
    }
  }
}
