package no.nav.bidrag.vedtak.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettEngangsbeløpRequestDto
import java.math.BigDecimal
import kotlin.reflect.full.memberProperties

@Entity
data class Engangsbeløp(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "engangsbeløp_id")
    val id: Int = 0,

    @ManyToOne
    @JoinColumn(name = "vedtak_id")
    val vedtak: Vedtak = Vedtak(),

    @Column(nullable = false, name = "type")
    val type: String = "",

    @Column(nullable = false, name = "sak_id")
    val sak: String = "",

    @Column(nullable = false, name = "skyldner_id")
    val skyldner: String = "",

    @Column(nullable = false, name = "kravhaver_id")
    val kravhaver: String = "",

    @Column(nullable = false, name = "mottaker_id")
    val mottaker: String = "",

    @Column(nullable = true, name = "beløp")
    val beløp: BigDecimal? = BigDecimal.ZERO,

    @Column(nullable = true, name = "valutakode")
    val valutakode: String? = "",

    @Column(nullable = false, name = "resultatkode")
    val resultatkode: String = "",

    @Column(nullable = false, name = "innkreving")
    val innkreving: String = "",

    @Column(nullable = false, name = "beslutning")
    val beslutning: String = "",

    @Column(nullable = true, name = "omgjør_vedtak_id")
    val omgjørVedtakId: Int? = 0,

    @Column(nullable = false, name = "referanse")
    val referanse: String = "",

    @Column(nullable = true, name = "delytelse_id")
    val delytelseId: String? = "",

    @Column(nullable = true, name = "ekstern_referanse")
    val eksternReferanse: String? = ""
)

fun OpprettEngangsbeløpRequestDto.toEngangsbeløpEntity(eksisterendeVedtak: Vedtak) = with(::Engangsbeløp) {
    val propertiesByName = OpprettEngangsbeløpRequestDto::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                Engangsbeløp::id.name -> 0
                Engangsbeløp::vedtak.name -> eksisterendeVedtak
                Engangsbeløp::type.name -> type.toString()
                Engangsbeløp::innkreving.name -> innkreving.toString()
                else -> propertiesByName[parameter.name]?.get(this@toEngangsbeløpEntity)
            }
        }
    )
}
