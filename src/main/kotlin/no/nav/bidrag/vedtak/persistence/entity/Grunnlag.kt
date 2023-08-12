package no.nav.bidrag.vedtak.persistence.entity

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import no.nav.bidrag.domain.enums.GrunnlagType
import no.nav.bidrag.transport.behandling.vedtak.response.GrunnlagDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettGrunnlagRequestDto
import kotlin.reflect.full.memberProperties

@Entity
data class Grunnlag(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grunnlag_id")
    val id: Int = 0,

    @Column(nullable = false, name = "referanse")
    val referanse: String = "",

    @ManyToOne
    @JoinColumn(name = "vedtak_id")
    val vedtak: Vedtak = Vedtak(),

    @Column(nullable = false, name = "type")
    val type: String = "",

    @Column(nullable = false, name = "innhold")
    val innhold: String = ""

)

fun OpprettGrunnlagRequestDto.toGrunnlagEntity(vedtak: Vedtak) = with(::Grunnlag) {
    val propertiesByName = OpprettGrunnlagRequestDto::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                Grunnlag::id.name -> 0
                Grunnlag::vedtak.name -> vedtak
                Grunnlag::type.name -> type.toString()
                Grunnlag::innhold.name -> innhold.toString()
                else -> propertiesByName[parameter.name]?.get(this@toGrunnlagEntity)
            }
        }
    )
}

fun Grunnlag.toGrunnlagDto() = with(::GrunnlagDto) {
    val propertiesByName = Grunnlag::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                GrunnlagDto::type.name -> GrunnlagType.valueOf(type)
                GrunnlagDto::innhold.name -> stringTilJsonNode(innhold)
                else -> propertiesByName[parameter.name]?.get(this@toGrunnlagDto)
            }
        }
    )
}

private fun stringTilJsonNode(innhold: String): JsonNode {
    val mapper = ObjectMapper()
    return mapper.readTree(innhold)
}
