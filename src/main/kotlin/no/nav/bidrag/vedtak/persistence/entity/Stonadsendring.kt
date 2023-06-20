package no.nav.bidrag.vedtak.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettStonadsendringRequestDto
import kotlin.reflect.full.memberProperties

@Entity
data class Stonadsendring(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stonadsendring_id")
    val id: Int = 0,

    @Column(nullable = false, name = "type")
    val type: String = "",

    @ManyToOne
    @JoinColumn(name = "vedtak_id")
    val vedtak: Vedtak = Vedtak(),

    @Column(nullable = false, name = "sak_id")
    val sakId: String = "",

    @Column(nullable = false, name = "skyldner_id")
    val skyldnerId: String = "",

    @Column(nullable = false, name = "kravhaver_id")
    val kravhaverId: String = "",

    @Column(nullable = false, name = "mottaker_id")
    val mottakerId: String = "",

    @Column(nullable = true, name = "indeksregulering_aar")
    val indeksreguleringAar: String? = "",

    @Column(nullable = false, name = "innkreving")
    val innkreving: String = "",

    @Column(nullable = false, name = "endring")
    val endring: Boolean = true,

    @Column(nullable = true, name = "omgjor_vedtak_id")
    val omgjorVedtakId: Int? = 0,

    @Column(nullable = true, name = "ekstern_referanse")
    val eksternReferanse: String? = ""

)

fun OpprettStonadsendringRequestDto.toStonadsendringEntity(eksisterendeVedtak: Vedtak) = with(::Stonadsendring) {
    val propertiesByName = OpprettStonadsendringRequestDto::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                Stonadsendring::id.name -> 0
                Stonadsendring::type.name -> type.toString()
                Stonadsendring::vedtak.name -> eksisterendeVedtak
                Stonadsendring::innkreving.name -> innkreving.toString()
                else -> propertiesByName[parameter.name]?.get(this@toStonadsendringEntity)
            }
        }
    )
}
