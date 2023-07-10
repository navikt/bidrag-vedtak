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
    @Column(name = "vedtak_id")
    val id: Int = 0,

    @Column(nullable = false, name = "kilde")
    val kilde: String = "",

    @Column(nullable = false, name = "type")
    val type: String = "",

    @Column(nullable = false, name = "enhet_id")
    val enhetId: String = "",

    @Column(nullable = false, name = "vedtak_tidspunkt")
    val vedtakTidspunkt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false, name = "opprettet_av")
    val opprettetAv: String = "",

    @Column(nullable = true, name = "opprettet_av_navn")
    val opprettetAvNavn: String? = "",

    @Column(nullable = false, name = "opprettet_timestamp")
    val opprettetTimestamp: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = true, name = "utsatt_til_dato")
    val utsattTilDato: LocalDate? = LocalDate.now()
)

fun OpprettVedtakRequestDto.toVedtakEntity() = with(::Vedtak) {
    val propertiesByName = OpprettVedtakRequestDto::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                Vedtak::id.name -> 0
                Vedtak::kilde.name -> kilde.toString()
                Vedtak::type.name -> type.toString()
                Vedtak::opprettetTimestamp.name -> LocalDateTime.now()
                else -> propertiesByName[parameter.name]?.get(this@toVedtakEntity)
            }
        }
    )
}
