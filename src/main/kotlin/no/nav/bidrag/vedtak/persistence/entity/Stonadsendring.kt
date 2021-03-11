package no.nav.bidrag.vedtak.persistence.entity

import no.nav.bidrag.vedtak.dto.StonadsendringDto
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import kotlin.reflect.full.memberProperties

@Entity
data class Stonadsendring (

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "stonadsendring_id")
  val stonadsendringId: Int = 0,

  @Column(nullable = false, name = "stonad_type")
  val stonadType: String = "",

  @ManyToOne
  @JoinColumn(name="vedtak_id")
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
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now()
)

fun Stonadsendring.toStonadsendringDto() = with(::StonadsendringDto) {
  val propertiesByName = Stonadsendring::class.memberProperties.associateBy { it.name }
  callBy(parameters.associate { parameter ->
    parameter to when (parameter.name) {
      StonadsendringDto::vedtakId.name -> vedtak.vedtakId
      else -> propertiesByName[parameter.name]?.get(this@toStonadsendringDto)
    }
  })
}
