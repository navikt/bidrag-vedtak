package no.nav.bidrag.vedtak.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettBehandlingsreferanseRequestDto
import kotlin.reflect.full.memberProperties

@Entity
data class Behandlingsreferanse(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "behandlingsreferanse_id")
    val id: Int = 0,

    @ManyToOne
    @JoinColumn(name = "vedtak_id")
    val vedtak: Vedtak = Vedtak(),

    @Column(nullable = false, name = "kilde")
    val kilde: String = "",

    @Column(nullable = false, name = "referanse")
    val referanse: String = ""
)

fun OpprettBehandlingsreferanseRequestDto.toBehandlingsreferanseEntity(eksisterendeVedtak: Vedtak) = with(::Behandlingsreferanse) {
    val propertiesByName = OpprettBehandlingsreferanseRequestDto::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                Behandlingsreferanse::id.name -> 0
                Behandlingsreferanse::kilde.name -> kilde.toString()
                Behandlingsreferanse::vedtak.name -> eksisterendeVedtak
                else -> propertiesByName[parameter.name]?.get(this@toBehandlingsreferanseEntity)
            }
        }
    )
}
