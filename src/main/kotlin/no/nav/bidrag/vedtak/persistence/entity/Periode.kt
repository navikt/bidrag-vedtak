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
class Periode {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "periode_id")
  var periodeId: Int? = null

  @Column(nullable = false, name = "periode_fom")
  lateinit var periodeFom: LocalDate

  @Column(nullable = false, name = "periode_tom")
  lateinit var periodeTom: LocalDate

  @Column(nullable = false, name = "stonad_id")
  var stonadId: Int? = null

  @Column(nullable = false, name = "belop")
  lateinit var belop: BigDecimal

  @Column(nullable = false, name = "valutakode")
  lateinit var valutakode: String

  @Column(nullable = false, name = "resultatkode")
  lateinit var resultatkode: String

  @Column(nullable = false, name = "opprettet_av")
  lateinit var opprettetAv: String

  @Column(nullable = false, name = "opprettet_timestamp")
  lateinit var opprettetTimestamp: LocalDateTime

}