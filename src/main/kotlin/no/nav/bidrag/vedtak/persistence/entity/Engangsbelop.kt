package no.nav.bidrag.vedtak.persistence.entity

import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettEngangsbelopRequestDto
import no.nav.bidrag.vedtak.bo.EngangsbelopBo
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import kotlin.reflect.full.memberProperties

@Entity
data class Engangsbelop(

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "engangsbelop_id")
  val engangsbelopId: Int = 0,

  @ManyToOne
  @JoinColumn(name = "vedtak_id")
  val vedtak: Vedtak = Vedtak(),

  @Column(nullable = false, name = "lopenr")
  val lopenr: Int = 0,

  @Column(nullable = false, name = "sak_id")
  val sakId: String = "",

  @Column(nullable = true, name = "endrer_engangsbelop_id")
  val endrerEngangsbelopId: Int? = 0,

  @Column(nullable = false, name = "type")
  val type: String = "",

  @Column(nullable = false, name = "skyldner_id")
  val skyldnerId: String = "",

  @Column(nullable = false, name = "kravhaver_id")
  val kravhaverId: String = "",

  @Column(nullable = false, name = "mottaker_id")
  val mottakerId: String = "",

  @Column(nullable = true, name = "belop")
  val belop: BigDecimal? = BigDecimal.ZERO,

  @Column(nullable = true, name = "valutakode")
  val valutakode: String? = "",

  @Column(nullable = false, name = "resultatkode")
  val resultatkode: String = "",

  @Column(nullable = true, name = "referanse")
  val referanse: String? = ""
)

fun OpprettEngangsbelopRequestDto.toEngangsbelopEntity(eksisterendeVedtak: Vedtak, lopenr: Int) = with(::Engangsbelop) {
  val propertiesByName = OpprettEngangsbelopRequestDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Engangsbelop::engangsbelopId.name -> 0
      Engangsbelop::vedtak.name -> eksisterendeVedtak
      Engangsbelop::lopenr.name -> lopenr
      Engangsbelop::type.name -> type.toString()
      else -> propertiesByName[parameter.name]?.get(this@toEngangsbelopEntity)
    }
  })
}

fun Engangsbelop.toEngangsbelopBo() = with(::EngangsbelopBo) {
  val propertiesByName = Engangsbelop::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toEngangsbelopBo)
    }
  })
}