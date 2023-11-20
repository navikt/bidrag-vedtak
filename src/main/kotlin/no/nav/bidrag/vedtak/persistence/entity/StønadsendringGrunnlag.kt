package no.nav.bidrag.vedtak.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettStønadsendringGrunnlagRequestDto
import kotlin.reflect.full.memberProperties

@IdClass(StønadsendringGrunnlagPK::class)
@Entity
@Table(name = "stønadsendringgrunnlag")
data class StønadsendringGrunnlag(

    @Id
    @ManyToOne
    @JoinColumn(name = "stønadsendringsid")
    val stønadsendring: Stønadsendring = Stønadsendring(),

    @Id
    @ManyToOne
    @JoinColumn(name = "grunnlagsid")
    val grunnlag: Grunnlag = Grunnlag(),

)

fun OpprettStønadsendringGrunnlagRequestDto.toStønadsendringGrunnlagEntity(eksisterendeStønadsendring: Stønadsendring, eksisterendeGrunnlag: Grunnlag) = with(::StønadsendringGrunnlag) {
    val propertiesByName = OpprettStønadsendringGrunnlagRequestDto::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                StønadsendringGrunnlag::stønadsendring.name -> eksisterendeStønadsendring
                StønadsendringGrunnlag::grunnlag.name -> eksisterendeGrunnlag
                else -> propertiesByName[parameter.name]?.get(this@toStønadsendringGrunnlagEntity)
            }
        },
    )
}

data class StønadsendringGrunnlagPK(val stønadsendring: Int = 0, val grunnlag: Int = 0)
