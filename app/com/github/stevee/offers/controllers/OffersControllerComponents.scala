package com.github.stevee.offers.controllers

import com.github.stevee.offers.service.OfferService
import javax.inject.Inject
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._


case class OffersControllerComponents @Inject()(
                                                 offerService: OfferService,
                                                 actionBuilder: DefaultActionBuilder,
                                                 parsers: PlayBodyParsers,
                                                 messagesApi: MessagesApi,
                                                 langs: Langs,
                                                 fileMimeTypes: FileMimeTypes,
                                                 executionContext: scala.concurrent.ExecutionContext)
  extends ControllerComponents