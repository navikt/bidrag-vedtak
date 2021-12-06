package no.nav.bidrag.vedtak.persistence.entity

import no.nav.bidrag.vedtak.dto.BehandlingsreferanseDto
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import kotlin.reflect.full.memberProperties

@Entity
data class Behandlingsreferanse(

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "behandlingsreferanse_id")
  val behandlingsreferanseId: Int = 0,

  @ManyToOne
  @JoinColumn(name = "vedtak_id")
  val vedtak: Vedtak = Vedtak(),

  @Column(nullable = false, name = "kilde")
  val kilde: String = "",

  @Column(nullable = false, name = "referanse")
  val referanse: String = ""
)

fun Behandlingsreferanse.toBehandlingsreferanseDto() = with(::BehandlingsreferanseDto) {
  val propertiesByName = Behandlingsreferanse::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      BehandlingsreferanseDto::vedtakId.name -> vedtak.vedtakId
      else -> propertiesByName[parameter.name]?.get(this@toBehandlingsreferanseDto)
    }
  })
}
