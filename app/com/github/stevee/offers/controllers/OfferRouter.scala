package com.github.stevee.offers.controllers


import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class OfferRouter @Inject()(controller: OfferController) extends SimpleRouter {
  val prefix = "/v1/offers"

  override def routes: Routes = {
    case DELETE(p"/$id") =>
      controller.cancel(id)

    case POST(p"/") =>
      controller.create

    case GET(p"/$id") =>
      controller.get(id)
  }

}
