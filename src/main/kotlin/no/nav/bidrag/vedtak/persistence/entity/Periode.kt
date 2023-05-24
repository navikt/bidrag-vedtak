package no.nav.bidrag.vedtak.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettVedtakPeriodeRequestDto
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

@Entity
data class Periode(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "periode_id")
    val id: Int = 0,

    @Column(nullable = false, name = "fom_dato")
    val fomDato: LocalDate = LocalDate.now(),

    @Column(nullable = true, name = "til_dato")
    val tilDato: LocalDate? = null,

    @ManyToOne
    @JoinColumn(name = "stonadsendring_id")
    val stonadsendring: Stonadsendring = Stonadsendring(),

    @Column(nullable = true, name = "belop")
    val belop: BigDecimal? = BigDecimal.ZERO,

    @Column(nullable = true, name = "valutakode")
    val valutakode: String? = "",

    @Column(nullable = false, name = "resultatkode")
    val resultatkode: String = "",

    @Column(nullable = true, name = "delytelse_id")
    val delytelseId: String? = ""
)

fun OpprettVedtakPeriodeRequestDto.toPeriodeEntity(eksisterendeStonadsendring: Stonadsendring) = with(::Periode) {
    val propertiesByName = OpprettVedtakPeriodeRequestDto::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                Periode::id.name -> 0
                Periode::stonadsendring.name -> eksisterendeStonadsendring
                else -> propertiesByName[parameter.name]?.get(this@toPeriodeEntity)
            }
        }
    )
}
