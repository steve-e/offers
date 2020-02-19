package com.github.stevee.offers.service

import java.time.{Clock, LocalDateTime, ZoneId, ZoneOffset}
import java.util.UUID

import com.github.stevee.offers.api.CreateOfferRequest
import com.github.stevee.offers.domain.Offer
import com.github.stevee.offers.repo.OfferRepository
import org.scalatestplus.mockito.MockitoSugar
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.Scope
import org.specs2.mock.Mockito._
import org.specs2.mutable.Specification

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class OfferServiceSpec(implicit ee: ExecutionEnv) extends Specification with MockitoSugar {

  "OfferService" should {
    "create a new offer" in new OfferServiceContext {
      val request =
        CreateOfferRequest(price = offer.price,
          currency = offer.currency,
          description = offer.description,
          start = now,
          duration = FiniteDuration(days, "days"))

      repoMock.add(offer).returns(Future.successful(offer))
      idServiceMock.newId returns id
      offerService.create(request) must beEqualTo(offer).await
    }

    "find a valid offer by id" in new OfferServiceContext {
      repoMock.get(id).returns(Future.successful(Some(offer)))
      offerService.findValid(id) must beEqualTo(FoundValid(offer)).await
    }

    "find a cancelled offer by id" in new OfferServiceContext {
      repoMock.get(id).returns(Future.successful(Some(offer.copy(cancelled = true))))
      offerService.findValid(id) must beEqualTo(FoundExpired).await
    }

    "find an expired offer by id" in new OfferServiceContext {
      val afterEndOfOffer = Clock.fixed(now.plusDays(days + 1).toInstant(ZoneOffset.UTC), zoneId)
      override val fixedClock = afterEndOfOffer

      repoMock.get(id).returns(Future.successful(Some(offer)))
      offerService.findValid(id) must beEqualTo(FoundExpired).await
    }

    "fail to find an offer" in new OfferServiceContext {
      repoMock.get(id).returns(Future.successful(None))
      offerService.findValid(id) must beEqualTo(OfferNotFound).await
    }

    "cancel an existing offer" in new OfferServiceContext {
      repoMock.get(id).returns(Future.successful(Some(offer)))
      repoMock.update(offer.copy(cancelled = true)).returns(Future.successful(true))
      offerService.cancel(id) must beTrue.await
    }

    "attempt to cancel a non-existing offer" in new OfferServiceContext {
      repoMock.get(id).returns(Future.successful(None))
      offerService.cancel(id) must beFalse.await
    }
  }

  trait OfferServiceContext extends Scope {

    val days = 10L
    val repoMock = mock[OfferRepository]
    val idServiceMock = mock[IdService]
    val zoneId: ZoneId = ZoneId.of("UTC")
    val now = LocalDateTime.now(zoneId)

    def fixedClock = Clock.fixed(now.toInstant(ZoneOffset.UTC), zoneId)

    lazy val clockProvider = new ClockProvider {
      override def clock: Clock = fixedClock
    }

    val id = UUID.randomUUID()

    val offer = Offer(id = id,
      price = BigDecimal("9.99"),
      currency = "GBP",
      description = "iPhone 11",
      start = now,
      end = now.plusDays(days),
      cancelled = false)

    lazy val offerService = new OfferService(repoMock, idServiceMock, clockProvider)
  }

}
