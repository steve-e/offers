package com.github.stevee.offers.controllers

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

import com.github.stevee.offers.api.{CreateOfferRequest, OfferResponse}
import com.github.stevee.offers.domain.Offer
import com.github.stevee.offers.service.{FoundExpired, FoundValid, OfferNotFound, OfferService}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import org.specs2.matcher.Scope
import org.specs2.mock.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future
import scala.concurrent.duration._

class OfferControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with MockitoSugar {

  import scala.concurrent.ExecutionContext.Implicits.global

  "OfferController create" should {

    "create an offer" in new OfferControllerScope {
      offerService.create(request) returns Future.successful(Offer(
        id = id,
        price = price,
        currency = "GBP",
        description = "iPhone", start = start,
        end = start.plusDays(10),
        cancelled = false))

      val eventualResult = controller.create()(fakeRequest
        .withJsonBody(createRequest)
      )
      contentAsString(eventualResult) must include(s""""id":"$id"""")
      status(eventualResult) mustBe CREATED
      contentType(eventualResult) mustBe Some("application/json")
    }

    "fail with 400 if price is not a valid format" in new OfferControllerScope {
      override val priceString = "£100"
      val eventualResult = controller.create()(fakeRequest.withJsonBody(createRequest))

      status(eventualResult) mustBe BAD_REQUEST
    }

    "fail with 400 if currency is not valid" in new OfferControllerScope {
      override val currency = "FOO"
      val eventualResult = controller.create()(fakeRequest.withJsonBody(createRequest))

      status(eventualResult) mustBe BAD_REQUEST
    }

    "fail with 400 if description is empty" in new OfferControllerScope {
      override val description = ""
      val eventualResult = controller.create()(fakeRequest.withJsonBody(createRequest))

      status(eventualResult) mustBe BAD_REQUEST
    }

    "fail with 400 if description is to long" in new OfferControllerScope {
      override val description = "Ha" * 1000
      val eventualResult = controller.create()(fakeRequest.withJsonBody(createRequest))

      status(eventualResult) mustBe BAD_REQUEST
    }

    "fail with 400 if start is not valid" in new OfferControllerScope {
      override val startString = "Boxing Day"
      val eventualResult = controller.create()(fakeRequest.withJsonBody(createRequest))

      status(eventualResult) mustBe BAD_REQUEST
    }

    "fail with 400 if duration is not valid" in new OfferControllerScope {
      override def duration: String = " a week and a half"

      val eventualResult = controller.create()(fakeRequest.withJsonBody(createRequest))

      status(eventualResult) mustBe BAD_REQUEST
    }
  }

  "OfferController get" should {

    "fail with 404 if the id does not exist" in new OfferControllerScope {
      offerService.findValid(id) returns Future.successful(OfferNotFound)
      val eventualResult = controller.get(id.toString)(fakeRequest)
      status(eventualResult) mustBe NOT_FOUND
    }

    "find a valid offer" in new OfferControllerScope {

      offerService.findValid(id) returns Future.successful(FoundValid(expectedOffer))
      val eventualResult = controller.get(id.toString)(fakeRequest)

      status(eventualResult) mustBe OK

      val offerResponse = contentAsJson(eventualResult).as[OfferResponse]
      offerResponse.id mustBe expectedOffer.id
      offerResponse.currency mustBe expectedOffer.currency
      offerResponse.description mustBe expectedOffer.description
      offerResponse.start mustBe expectedOffer.start
      offerResponse.end mustBe expectedOffer.end
    }

    "get an expired offer" in new OfferControllerScope {
      offerService.findValid(id) returns Future.successful(FoundExpired)

      val eventualResult = controller.get(id.toString)(fakeRequest)
      status(eventualResult) mustBe GONE
    }
  }

  "OfferController cancel" should {
    "fail with 404 if the id does not exist" in new OfferControllerScope {
      offerService.cancel(id) returns Future.successful(false)
      val eventualResult = controller.cancel(id.toString)(fakeRequest)
      status(eventualResult) mustBe NOT_FOUND
    }

    "cancel an offer" in new OfferControllerScope {
      offerService.cancel(id) returns Future.successful(true)
      val cancelResult = controller.cancel(id.toString)(fakeRequest)
      status(cancelResult) mustBe OK
    }
  }

  trait OfferControllerScope extends Scope {

    val fakeRequest = FakeRequest().withHeaders("Content-Type" -> "application/json")
    val id: UUID = UUID.randomUUID()
    val components = inject[OffersControllerComponents]
    val offerService = mock[OfferService]
    val controller = new OfferController(components.copy(offerService = offerService))

    def start: LocalDateTime = LocalDate.of(2020, 2, 20).atStartOfDay()

    def currency = "GBP"

    def description = "iPhone"

    def duration = "10 days"

    def priceString = "9.99"

    def startString = "2020-02-20T00:00:00"

    lazy val price = BigDecimal(priceString)

    lazy val request = CreateOfferRequest(
      price = price,
      currency = currency,
      description = description,
      start = start,
      duration = Duration(duration))

    lazy val expectedOffer = Offer(id = id,
      price = price,
      currency = currency,
      description = description,
      start = start,
      end = start.plusDays(10),
      cancelled = false
    )

    lazy val requestBody =
      s"""
         |{
         |"price":"$priceString",
         |"currency":  "$currency" ,
         |"description":"$description",
         |"start":"$startString",
         |"duration":"$duration"
         |}
         |""".stripMargin.trim

    lazy val createRequest = Json.parse(requestBody)
  }

}
