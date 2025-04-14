package no.nav.bidrag.vedtak.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettVedtakRequestDto
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

@Entity
data class Vedtak(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vedtaksid")
    var id: Int = 0,

    @Column(nullable = false, name = "kilde")
    val kilde: String = "",

    @Column(nullable = false, name = "type")
    val type: String = "",

    @Column(nullable = false, name = "opprettet_av")
    val opprettetAv: String = "",

    @Column(nullable = true, name = "opprettet_av_navn")
    val opprettetAvNavn: String? = null,

    @Column(nullable = false, name = "kildeapplikasjon")
    val kildeapplikasjon: String = "",

    @Column(nullable = true, name = "vedtakstidspunkt")
    var vedtakstidspunkt: LocalDateTime? = LocalDateTime.now(),

    @Column(nullable = true, name = "unik_referanse")
    val unikReferanse: String? = null,

    @Column(nullable = true, name = "enhetsnummer")
    val enhetsnummer: String? = "",

    @Column(nullable = true, name = "innkreving_utsatt_til_dato")
    val innkrevingUtsattTilDato: LocalDate? = LocalDate.now(),

    @Column(nullable = true, name = "fastsatt_i_land")
    val fastsattILand: String? = "",

    @Column(nullable = false, name = "opprettet_tidspunkt")
    val opprettetTidspunkt: LocalDateTime = LocalDateTime.now(),

)

fun OpprettVedtakRequestDto.toVedtakEntity(
    opprettetAv: String,
    opprettetAvNavn: String?,
    kildeapplikasjon: String,
    vedtakstidspunkt: LocalDateTime?,
) = with(::Vedtak) {
    val propertiesByName = OpprettVedtakRequestDto::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                Vedtak::id.name -> 0
                Vedtak::kilde.name -> kilde.toString()
                Vedtak::type.name -> type.toString()
                Vedtak::opprettetAv.name -> opprettetAv
                Vedtak::opprettetAvNavn.name -> opprettetAvNavn
                Vedtak::vedtakstidspunkt.name -> vedtakstidspunkt
                Vedtak::kildeapplikasjon.name -> kildeapplikasjon
                Vedtak::opprettetTidspunkt.name -> LocalDateTime.now()
                Vedtak::enhetsnummer.name -> enhetsnummer?.toString()
                else -> propertiesByName[parameter.name]?.get(this@toVedtakEntity)
            }
        },
    )
}
