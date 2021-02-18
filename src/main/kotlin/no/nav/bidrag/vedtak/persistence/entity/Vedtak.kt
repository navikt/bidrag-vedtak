package no.nav.bidrag.vedtak.persistence.entity

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Vedtak() {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var vedtak_id: Int? = null

  @Column(nullable = false)
  lateinit var opprettet_av: String

  @Column(nullable = false)
  lateinit var opprettet_timestamp: LocalDateTime

  @Column(nullable = false)
  lateinit var enhetsnummer: String
}
