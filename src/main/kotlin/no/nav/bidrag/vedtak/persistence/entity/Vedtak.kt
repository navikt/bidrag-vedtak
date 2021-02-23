package no.nav.bidrag.vedtak.persistence.entity

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Vedtak {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "vedtak_id")
  var vedtakId: Int? = null

  @Column(nullable = false, name = "opprettet_av")
  lateinit var opprettetAv: String

  @Column(nullable = false, name = "opprettet_timestamp")
  lateinit var opprettetTimestamp: LocalDateTime

  @Column(nullable = false)
  lateinit var enhetsnummer: String
}
