package no.nav.bidrag.vedtak.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import no.nav.bidrag.domene.enums.Beslutningstype
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettStønadsendringRequestDto
import kotlin.reflect.full.memberProperties

@Entity
data class Stønadsendring(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stønadsendring_id")
    val id: Int = 0,

    @Column(nullable = false, name = "type")
    val type: String = "",

    @ManyToOne
    @JoinColumn(name = "vedtak_id")
    val vedtak: Vedtak = Vedtak(),

    @Column(nullable = false, name = "sak_id")
    val sak: String = "",

    @Column(nullable = false, name = "skyldner_id")
    val skyldner: String = "",

    @Column(nullable = false, name = "kravhaver_id")
    val kravhaver: String = "",

    @Column(nullable = false, name = "mottaker_id")
    val mottaker: String = "",

    @Column(nullable = true, name = "indeksregulering_aar")
    val førsteIndeksreguleringsår: Int? = 0,

    @Column(nullable = false, name = "innkreving")
    val innkreving: String = "",

    @Column(nullable = false, name = "beslutning")
    val beslutning: String = "",

    @Column(nullable = true, name = "omgjør_vedtak_id")
    val omgjørVedtakId: Int? = 0,

    @Column(nullable = true, name = "ekstern_referanse")
    val eksternReferanse: String? = ""

)

fun OpprettStønadsendringRequestDto.toStønadsendringEntity(eksisterendeVedtak: Vedtak) = with(::Stønadsendring) {
    val propertiesByName = OpprettStønadsendringRequestDto::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                Stønadsendring::id.name -> 0
                Stønadsendring::type.name -> type.toString()
                Stønadsendring::vedtak.name -> eksisterendeVedtak
                Stønadsendring::innkreving.name -> innkreving.toString()
                else -> propertiesByName[parameter.name]?.get(this@toStønadsendringEntity)
            }
        }
    )
}
