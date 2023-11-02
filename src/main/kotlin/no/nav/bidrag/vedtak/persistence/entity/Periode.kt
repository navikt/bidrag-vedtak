package no.nav.bidrag.vedtak.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettPeriodeRequestDto
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
    @JoinColumn(name = "stønadsendring_id")
    val stønadsendring: Stønadsendring = Stønadsendring(),

    @Column(nullable = true, name = "beløp")
    val beløp: BigDecimal? = BigDecimal.ZERO,

    @Column(nullable = true, name = "valutakode")
    val valutakode: String? = "",

    @Column(nullable = false, name = "resultatkode")
    val resultatkode: String = "",

    @Column(nullable = true, name = "delytelse_id")
    val delytelseId: String? = ""
)

fun OpprettPeriodeRequestDto.toPeriodeEntity(eksisterendeStønadsendring: Stønadsendring) = with(::Periode) {
    val propertiesByName = OpprettPeriodeRequestDto::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                Periode::id.name -> 0
                Periode::stønadsendring.name -> eksisterendeStønadsendring
                else -> propertiesByName[parameter.name]?.get(this@toPeriodeEntity)
            }
        }
    )
}
