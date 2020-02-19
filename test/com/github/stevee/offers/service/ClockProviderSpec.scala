package com.github.stevee.offers.service

import java.time.Clock

import org.specs2.mutable.Specification

class ClockProviderSpec extends Specification {

  val clockProvider = new SystemClockProvider

  "clock provider returns UTC clock" in {
    val clock: Clock = clockProvider.clock

    val before = System.currentTimeMillis()
    Thread.sleep(1)
    val clockMillis = clock.millis()
    Thread.sleep(1)
    val after = System.currentTimeMillis()
    clockMillis must beGreaterThan(before)
    clockMillis must beLessThan(after)
  }
}
