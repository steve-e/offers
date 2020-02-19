package com.github.stevee.offers.domain

import java.time.LocalDateTime
import java.util.UUID


case class Offer(id: UUID, price: BigDecimal, currency: String, description: String, start: LocalDateTime, end: LocalDateTime, cancelled: Boolean)
