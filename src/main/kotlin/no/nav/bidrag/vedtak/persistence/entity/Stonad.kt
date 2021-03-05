package no.nav.bidrag.vedtak.persistence.entity

import no.nav.bidrag.vedtak.dto.StonadDto
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import kotlin.reflect.full.memberProperties

@Entity
data class Stonad (

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "stonad_id")
  val stonadId: Int = 0,

  @Column(nullable = false, name = "stonad_type")
  val stonadType: String = "",

  @ManyToOne
  val vedtak: Vedtak = Vedtak(),

  @Column(nullable = false, name = "behandling_id")
  val behandlingId: String = "",

  @Column(nullable = false, name = "skyldner_id")
  val skyldnerId: String = "",

  @Column(nullable = false, name = "kravhaver_id")
  val kravhaverId: String = "",

  @Column(nullable = false, name = "mottaker_id")
  val mottakerId: String = "",

  @Column(nullable = false, name = "opprettet_av")
  val opprettetAv: String = "",

  @Column(nullable = false, name = "opprettet_timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now(),

  @Column(nullable = false)
  val enhetsnummer: Int = 0
)

fun Stonad.toStonadDto() = with(::StonadDto) {
  val propertiesByName = Stonad::class.memberProperties.associateBy { it.name }
  callBy(parameters.associate { parameter ->
    parameter to when (parameter.name) {
      StonadDto::vedtakId.name -> vedtak.vedtakId
      else -> propertiesByName[parameter.name]?.get(this@toStonadDto)
    }
  })
}
