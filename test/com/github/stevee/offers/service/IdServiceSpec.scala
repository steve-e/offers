package com.github.stevee.offers.service

import org.specs2.mutable.Specification

class IdServiceSpec extends Specification {

  "newId returns a new unique id on each call" in {
    val idService = new IdService
    idService.newId mustNotEqual idService.newId
  }
}
