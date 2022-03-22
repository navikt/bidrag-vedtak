package no.nav.bidrag.vedtak.api.grunnlag

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.behandling.felles.enums.GrunnlagType
import no.nav.bidrag.vedtak.dto.GrunnlagDto
import javax.validation.constraints.NotBlank
import kotlin.reflect.full.memberProperties

@Schema
data class OpprettGrunnlagRequest(

    @Schema(description = "Referanse til grunnlaget")
    @NotBlank
    val referanse: String,

    @Schema(description = "Grunnlagstype")
    @NotBlank
    val type: GrunnlagType,

    @Schema(description = "Innholdet i grunnlaget")
    @NotBlank
    val innhold: JsonNode = ObjectMapper().createObjectNode()
)

fun OpprettGrunnlagRequest.toGrunnlagDto(vedtakId: Int) = with(::GrunnlagDto) {
    val propertiesByName = OpprettGrunnlagRequest::class.memberProperties.associateBy { it.name }
    callBy(parameters.associateWith { parameter ->
        when (parameter.name) {
            GrunnlagDto::vedtakId.name -> vedtakId
            GrunnlagDto::grunnlagId.name -> 0
            GrunnlagDto::type.name -> type.toString()
            GrunnlagDto::innhold.name -> innhold.toString()
            else -> propertiesByName[parameter.name]?.get(this@toGrunnlagDto)
        }
    })
}