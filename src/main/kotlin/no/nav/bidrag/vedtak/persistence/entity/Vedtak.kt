package no.nav.bidrag.vedtak.persistence.entity

import no.nav.bidrag.vedtak.dto.VedtakDto
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import kotlin.reflect.full.memberProperties

@Entity
data class Vedtak (

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "vedtak_id")
  val vedtakId: Int = 0,

  @Column(nullable = false)
  val enhetsnummer: String = "",

  @Column(nullable = false, name = "opprettet_av")
  val opprettetAv: String = "",

  @Column(nullable = false, name = "opprettet_timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now()
)

fun Vedtak.toVedtakDto() = with(::VedtakDto) {
  val propertiesByName = Vedtak::class.memberProperties.associateBy { it.name }
  callBy(parameters.associate { parameter ->
    parameter to when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toVedtakDto)
    }
  })
}
