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
    val id: Int = 0,

    @Column(nullable = false, name = "kilde")
    val kilde: String = "",

    @Column(nullable = false, name = "type")
    val type: String = "",

    @Column(nullable = false, name = "opprettet_av")
    val opprettetAv: String = "",

    @Column(nullable = true, name = "opprettet_av_navn")
    val opprettetAvNavn: String? = "",

    @Column(nullable = false, name = "vedtakstidspunkt")
    val vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false, name = "enhet_id")
    val enhetsnummer: String = "",

    @Column(nullable = true, name = "utsatt_til_dato")
    val innkrevingUtsattTilDato: LocalDate? = LocalDate.now(),

    @Column(nullable = true, name = "fastsatt_i_land")
    val fastsattILand: String? = "",

    @Column(nullable = false, name = "opprettet_tidspunkt")
    val opprettetTidspunkt: LocalDateTime = LocalDateTime.now()

)

fun OpprettVedtakRequestDto.toVedtakEntity() = with(::Vedtak) {
    val propertiesByName = OpprettVedtakRequestDto::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                Vedtak::id.name -> 0
                Vedtak::kilde.name -> kilde.toString()
                Vedtak::type.name -> type.toString()
                Vedtak::opprettetTidspunkt.name -> LocalDateTime.now()
                Vedtak::enhetsnummer.name -> enhetsnummer.toString()
                else -> propertiesByName[parameter.name]?.get(this@toVedtakEntity)
            }
        }
    )
}
