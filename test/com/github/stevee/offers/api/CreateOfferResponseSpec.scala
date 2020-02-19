package com.github.stevee.offers.api

import java.util.UUID

import org.specs2.mutable.Specification
import play.api.libs.json.Json

class CreateOfferResponseSpec extends Specification {

  val id: UUID = UUID.randomUUID()

  val responseBody = s"""{ "id" : "$id" }"""

  "CreateOfferRequest" should {
    "be rendered as json string" in {
       val serialized = Json.toJson(CreateOfferResponse(id.toString))
      val parsed = Json.parse(responseBody)
      serialized mustEqual parsed
    }
  }

}
