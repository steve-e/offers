package com.github.stevee.offers.service

import java.time.{Clock, ZoneOffset}
import java.util.UUID

import com.github.stevee.offers.api.CreateOfferRequest
import com.github.stevee.offers.domain.Offer
import com.github.stevee.offers.repo.OfferRepository
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

sealed trait FindOfferResult

case class FoundValid(offer: Offer) extends FindOfferResult

case object FoundExpired extends FindOfferResult

case object OfferNotFound extends FindOfferResult


@Singleton
class OfferService @Inject()(val offerRepository: OfferRepository, val idService: IdService, val clockProvider: ClockProvider)(implicit val ec: ExecutionContext) {
  import clockProvider.clock
  def create(offerRequest: CreateOfferRequest): Future[Offer] = {
    val offer = Offer(
      id = idService.newId,
      price = offerRequest.price,
      currency = offerRequest.currency,
      description = offerRequest.description,
      start = offerRequest.start,
      end = offerRequest.start.plusSeconds(offerRequest.duration.toSeconds),
      cancelled = false
    )
    offerRepository.add(offer)
  }

  def findValid(id: UUID): Future[FindOfferResult] =
    offerRepository
      .get(id)
      .map(opt => opt.fold[FindOfferResult](OfferNotFound)(o => findOfferResult(o)))

  private def findOfferResult(offer: Offer): FindOfferResult = {
    if (offer.cancelled || clock.instant().isAfter(offer.end.toInstant(ZoneOffset.UTC))) FoundExpired else FoundValid(offer)
  }

  def cancel(id: UUID): Future[Boolean] = {
    offerRepository.get(id).flatMap {
      case Some(offer) => offerRepository.update(offer.copy(cancelled = true))
      case None => Future.successful(false)
    }
  }
}

@ImplementedBy(classOf[SystemClockProvider])
trait ClockProvider {
  def clock:Clock
}

class SystemClockProvider extends ClockProvider {
  override def clock: Clock = Clock.systemUTC()
}