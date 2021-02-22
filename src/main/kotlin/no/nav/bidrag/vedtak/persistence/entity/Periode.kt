package no.nav.bidrag.vedtak.persistence.entity

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Periode() {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var periode_id: Int? = null

  @Column(nullable = false)
  lateinit var periode_fom: LocalDate

  @Column(nullable = false)
  lateinit var periode_tom: LocalDate

  @Column(nullable = false)
  var stonad_id: Int? = null

  @Column(nullable = false)
  lateinit var belop: BigDecimal

  @Column(nullable = false)
  lateinit var opprettet_av: String

  @Column(nullable = false)
  lateinit var opprettet_timestamp: LocalDateTime

  @Column(nullable = false)
  lateinit var enhetsnummer: String
}