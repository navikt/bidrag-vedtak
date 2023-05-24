package no.nav.bidrag.vedtak.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettVedtakPeriodeGrunnlagRequestDto
import kotlin.reflect.full.memberProperties

@IdClass(PeriodeGrunnlagPK::class)
@Entity
@Table(name = "periodegrunnlag")
data class PeriodeGrunnlag(

    @Id
    @ManyToOne
    @JoinColumn(name = "periode_id")
    val periode: Periode = Periode(),

    @Id
    @ManyToOne
    @JoinColumn(name = "grunnlag_id")
    val grunnlag: Grunnlag = Grunnlag()

)

fun OpprettVedtakPeriodeGrunnlagRequestDto.toPeriodeGrunnlagEntity(eksisterendePeriode: Periode, eksisterendeGrunnlag: Grunnlag) = with(::PeriodeGrunnlag) {
    val propertiesByName = OpprettVedtakPeriodeGrunnlagRequestDto::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                PeriodeGrunnlag::periode.name -> eksisterendePeriode
                PeriodeGrunnlag::grunnlag.name -> eksisterendeGrunnlag
                else -> propertiesByName[parameter.name]?.get(this@toPeriodeGrunnlagEntity)
            }
        }
    )
}

data class PeriodeGrunnlagPK(val periode: Int = 0, val grunnlag: Int = 0)
