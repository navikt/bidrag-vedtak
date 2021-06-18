package no.nav.bidrag.vedtak.api.grunnlag

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.dto.GrunnlagDto
import kotlin.reflect.full.memberProperties

@Schema
data class OpprettGrunnlagRequest(

    @Schema(description = "Referanse til grunnlaget")
    val grunnlagReferanse: String = "",

    @Schema(description = "Vedtak-id")
    val vedtakId: Int = 0,

    @Schema(description = "Grunnlagstype")
    val grunnlagType: String = "",

    @Schema(description = "Innholdet i grunnlaget")
    val grunnlagInnhold: JsonNode = ObjectMapper().createObjectNode()
)

fun OpprettGrunnlagRequest.toGrunnlagDto(vedtakId: Int) = with(::GrunnlagDto) {
    val propertiesByName = OpprettGrunnlagRequest::class.memberProperties.associateBy { it.name }
    callBy(parameters.associateWith { parameter ->
        when (parameter.name) {
            GrunnlagDto::vedtakId.name -> vedtakId
            GrunnlagDto::grunnlagId.name -> 0
            GrunnlagDto::grunnlagInnhold.name -> grunnlagInnhold.toString()
            else -> propertiesByName[parameter.name]?.get(this@toGrunnlagDto)
        }
    })
}

fun OpprettGrunnlagRequest.toGrunnlagDto() = with(::GrunnlagDto) {
    val propertiesByName = OpprettGrunnlagRequest::class.memberProperties.associateBy { it.name }
    callBy(parameters.associateWith { parameter ->
        when (parameter.name) {
            GrunnlagDto::grunnlagId.name -> 0
            GrunnlagDto::grunnlagInnhold.name -> grunnlagInnhold.toString()
            else -> propertiesByName[parameter.name]?.get(this@toGrunnlagDto)
        }
    })
}
