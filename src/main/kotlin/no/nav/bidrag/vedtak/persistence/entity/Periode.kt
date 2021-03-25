package no.nav.bidrag.vedtak.persistence.entity

import no.nav.bidrag.vedtak.dto.PeriodeDto
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import kotlin.reflect.full.memberProperties

@Entity
data class Periode(

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "periode_id")
  val periodeId: Int = 0,

  @Column(nullable = false, name = "periode_fom")
  val periodeFom: LocalDate = LocalDate.now(),

  @Column(nullable = false, name = "periode_tom")
  val periodeTom: LocalDate = LocalDate.now(),

  @ManyToOne
  @JoinColumn(name = "stonadsendring_id")
  val stonadsendring: Stonadsendring = Stonadsendring(),

  @Column(nullable = false, name = "belop")
  val belop: BigDecimal = BigDecimal.ZERO,

  @Column(nullable = false, name = "valutakode")
  val valutakode: String = "",

  @Column(nullable = false, name = "resultatkode")
  val resultatkode: String = ""
)

  fun Periode.toPeriodeDto() = with(::PeriodeDto) {
    val propertiesByName = Periode::class.memberProperties.associateBy { it.name }
    callBy(parameters.associate { parameter ->
      parameter to when (parameter.name) {
        PeriodeDto::stonadsendringId.name -> stonadsendring.stonadsendringId
        else -> propertiesByName[parameter.name]?.get(this@toPeriodeDto)
      }
    })

}