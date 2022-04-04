package no.nav.bidrag.vedtak.persistence.entity

import no.nav.bidrag.vedtak.bo.VedtakBo
import java.time.LocalDate
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

  @Column(nullable = false, name = "vedtak_type")
  val vedtakType: String = "",

  @Column(nullable = false, name = "enhet_id")
  val enhetId: String = "",

  @Column(nullable = true, name = "vedtak_dato")
  val vedtakDato: LocalDate? = null,

  @Column(nullable = false, name = "opprettet_av")
  val opprettetAv: String = "",

  @Column(nullable = false, name = "opprettet_timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now()
)

fun Vedtak.toVedtakDto() = with(::VedtakBo) {
  val propertiesByName = Vedtak::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      VedtakBo::vedtakType.name -> vedtakType.toString()
      else -> propertiesByName[parameter.name]?.get(this@toVedtakDto)
    }
  })
}
