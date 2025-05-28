package no.nav.bidrag.vedtak.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettStønadsendringRequestDto
import kotlin.reflect.full.memberProperties

@Entity
data class Stønadsendring(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stønadsendringsid")
    val id: Int = 0,

    @ManyToOne
    @JoinColumn(name = "vedtaksid")
    val vedtak: Vedtak = Vedtak(),

    @Column(nullable = false, name = "type")
    val type: String = "",

    @Column(nullable = false, name = "sak")
    val sak: String = "",

    @Column(nullable = false, name = "skyldner")
    val skyldner: String = "",

    @Column(nullable = false, name = "kravhaver")
    val kravhaver: String = "",

    @Column(nullable = false, name = "mottaker")
    val mottaker: String = "",

    @Column(nullable = true, name = "første_indeksreguleringsår")
    val førsteIndeksreguleringsår: Int? = 0,

    @Column(nullable = false, name = "innkreving")
    val innkreving: String = "",

    @Column(nullable = false, name = "beslutning")
    val beslutning: String = "",

    @Column(nullable = true, name = "omgjør_vedtak_id")
    val omgjørVedtakId: Int? = 0,

    @Column(nullable = true, name = "ekstern_referanse")
    val eksternReferanse: String? = "",

    @Column(nullable = true, name = "siste_vedtaksid")
    val sisteVedtaksid: Int? = 0,

)

fun OpprettStønadsendringRequestDto.toStønadsendringEntity(eksisterendeVedtak: Vedtak) = with(::Stønadsendring) {
    val propertiesByName = OpprettStønadsendringRequestDto::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                Stønadsendring::id.name -> 0
                Stønadsendring::vedtak.name -> eksisterendeVedtak
                Stønadsendring::type.name -> type.toString()
                Stønadsendring::sak.name -> sak.toString()
                Stønadsendring::skyldner.name -> skyldner.verdi
                Stønadsendring::kravhaver.name -> kravhaver.verdi
                Stønadsendring::mottaker.name -> mottaker.verdi
                Stønadsendring::innkreving.name -> innkreving.toString()
                Stønadsendring::beslutning.name -> beslutning.toString()
                Stønadsendring::sisteVedtaksid.name -> sisteVedtaksid.toString()
                else -> propertiesByName[parameter.name]?.get(this@toStønadsendringEntity)
            }
        },
    )
}
