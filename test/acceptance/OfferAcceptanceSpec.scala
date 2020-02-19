package acceptance

import java.time.LocalDate
import java.util.UUID

import com.github.stevee.offers.api.{CreateOfferResponse, OfferResponse}
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._


class OfferAcceptanceSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  val requestBody =
    """
      |{
      |"price":"9.99",
      |"currency":  "GBP" ,
      |"description":"iPhone",
      |"start":"2020-02-20T00:00:00",
      |"duration":"10 days"
      |}
      |""".stripMargin.trim
  val createRequest = Json.parse(requestBody)

  "OfferController POST" should {
    "create an offer" in new WithApplication() {
      val eventualResult  = route(app, FakeRequest(POST, "/v1/offers")
        .withJsonBody(createRequest)
        .withHeaders("Content-Type" -> "application/json")
      ).get

      contentAsString(eventualResult) must include(""""id":""")
      status(eventualResult) mustBe CREATED
      contentType(eventualResult) mustBe Some("application/json")
    }
  }

  "OfferController GET" should {
    "fails with 404 if the id is not a valid uuid" in new WithApplication() {
      val eventualResult = route(app, FakeRequest(GET, "/v1/offers/notauuid")).get
      status(eventualResult) mustBe NOT_FOUND
    }

    "fails with 404 if the id does not exist" in new WithApplication() {
      val eventualResult = route(app, FakeRequest(GET, s"/v1/offers/${UUID.randomUUID()}")).get
      status(eventualResult) mustBe NOT_FOUND
    }

    "finds a valid offer" in new WithApplication() {
      val createResult = route(app, FakeRequest(POST, "/v1/offers")
        .withJsonBody(createRequest)
        .withHeaders("Content-Type" -> "application/json")
      ).get
      val id = Json.fromJson[CreateOfferResponse](contentAsJson(createResult))
        .getOrElse(fail("Could not parse response")).id

      val eventualResult = route(app, FakeRequest(GET, s"/v1/offers/$id")).get

      status(eventualResult) mustBe OK

      val offer = contentAsJson(eventualResult).as[OfferResponse]
      offer.id.toString mustBe id
      offer.price mustBe BigDecimal("9.99")
      offer.description mustBe "iPhone"
      val expectedStartDate = LocalDate.of(2020, 2, 20)
      offer.start.toLocalDate mustBe expectedStartDate
      offer.end.toLocalDate mustBe expectedStartDate.plusDays(10)
    }

    "get an expired offer" in new WithApplication() {
      val createResult = route(app, FakeRequest(POST, "/v1/offers")
        .withJsonBody(createRequest)
        .withHeaders("Content-Type" -> "application/json")
      ).get

      val id = (contentAsJson(createResult) \ "id").get.as[String]
      val cancelResult = route(app, FakeRequest(DELETE, s"/v1/offers/$id")).get
      status(cancelResult) mustBe OK

      val eventualResult = route(app, FakeRequest(GET, s"/v1/offers/$id")).get
      status(eventualResult) mustBe GONE
    }
  }

  "OfferController DELETE" should {
    "fails with 404 if the id does not exist" in new WithApplication() {
      val eventualResult = route(app, FakeRequest(DELETE, "/v1/offers/foo")).get
      status(eventualResult) mustBe NOT_FOUND
    }

    "cancels an offer" in new WithApplication() {
      val createResult = route(app, FakeRequest(POST, "/v1/offers")
        .withJsonBody(createRequest)
        .withHeaders("Content-Type" -> "application/json")
      ).get

      val id = (contentAsJson(createResult) \ "id").get.as[String]

      val cancelResult = route(app, FakeRequest(DELETE, s"/v1/offers/$id")).get
      status(cancelResult) mustBe OK
    }
  }
}
