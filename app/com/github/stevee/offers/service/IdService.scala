package com.github.stevee.offers.service

import java.util.UUID


class IdService {
  def newId: UUID = UUID.randomUUID()
}