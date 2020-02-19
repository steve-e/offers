package com.github.stevee.offers.api

import java.time.LocalDateTime

import scala.concurrent.duration.Duration

case class CreateOfferRequest(price: BigDecimal,
                              currency: String,
                              description: String,
                              start: LocalDateTime,
                              duration: Duration)

