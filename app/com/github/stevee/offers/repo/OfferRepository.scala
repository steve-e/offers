package com.github.stevee.offers.repo

import java.util.UUID

import com.github.stevee.offers.domain.Offer
import com.google.inject.ImplementedBy

import scala.collection.mutable
import scala.concurrent.Future

@ImplementedBy(classOf[StubOfferRepository])
trait OfferRepository {

  def add(offer: Offer): Future[Offer]

  def get(id: UUID): Future[Option[Offer]]

  def update(offer: Offer): Future[Boolean]
}

class StubOfferRepository extends OfferRepository {
  val store: mutable.HashMap[UUID, Offer] = new mutable.HashMap[UUID, Offer]()

  override def add(offer: Offer): Future[Offer] = {
    store.addOne(offer.id -> offer)
    Future.successful(offer)
  }

  override def get(id: UUID): Future[Option[Offer]] = {
    Future.successful(store.get(id))
  }

  override def update(offer: Offer): Future[Boolean] = {
    Future.successful(store.get(offer.id).fold(false) { _ =>
      store.update(offer.id, offer)
      true
    }
    )
  }
}
