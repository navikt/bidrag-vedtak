package no.nav.bidrag.vedtak.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettEngangsbelopRequestDto
import java.math.BigDecimal
import kotlin.reflect.full.memberProperties

@Entity
data class Engangsbelop(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "engangsbelop_id")
    val id: Int = 0,

    @ManyToOne
    @JoinColumn(name = "vedtak_id")
    val vedtak: Vedtak = Vedtak(),

    @Column(nullable = false, name = "type")
    val type: String = "",

    @Column(nullable = false, name = "sak_id")
    val sakId: String = "",

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

    @Column(nullable = false, name = "innkreving")
    val innkreving: String = "",

    @Column(nullable = false, name = "endring")
    val endring: Boolean = true,

    @Column(nullable = true, name = "omgjor_vedtak_id")
    val omgjorVedtakId: Int? = 0,

    @Column(nullable = false, name = "referanse")
    val referanse: String = "",

    @Column(nullable = true, name = "delytelse_id")
    val delytelseId: String? = "",

    @Column(nullable = true, name = "ekstern_referanse")
    val eksternReferanse: String? = ""
)

fun OpprettEngangsbelopRequestDto.toEngangsbelopEntity(eksisterendeVedtak: Vedtak) = with(::Engangsbelop) {
    val propertiesByName = OpprettEngangsbelopRequestDto::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                Engangsbelop::id.name -> 0
                Engangsbelop::vedtak.name -> eksisterendeVedtak
                Engangsbelop::type.name -> type.toString()
                Engangsbelop::innkreving.name -> innkreving.toString()
                else -> propertiesByName[parameter.name]?.get(this@toEngangsbelopEntity)
            }
        }
    )
}
