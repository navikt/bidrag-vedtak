package no.nav.bidrag.vedtak.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.vedtak.ISSUER
import no.nav.bidrag.vedtak.api.engangsbelopgrunnlag.OpprettEngangsbelopGrunnlagRequest
import no.nav.bidrag.vedtak.dto.EngangsbelopGrunnlagDto
import no.nav.bidrag.vedtak.service.EngangsbelopGrunnlagService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = ISSUER)
class EngangsbelopGrunnlagController(private val engangsbelopGrunnlagService: EngangsbelopGrunnlagService) {

  @PostMapping(OPPRETT_ENGANGSBELOPGRUNNLAG)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary ="Oppretter nytt engangsbelopgrunnlag")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Engangsbelopgrunnlag opprettet"),
      ApiResponse(responseCode = "400", description = "Feil opplysinger oppgitt", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))])
    ]
  )
  fun opprettEngangsbelopGrunnlag(@RequestBody request: OpprettEngangsbelopGrunnlagRequest): ResponseEntity<EngangsbelopGrunnlagDto>? {
    val engangsbelopGrunnlagOpprettet = engangsbelopGrunnlagService.opprettEngangsbelopGrunnlag(request)
    LOGGER.info("Følgende engangsbelopgrunnlag er opprettet: $engangsbelopGrunnlagOpprettet")
    return ResponseEntity(engangsbelopGrunnlagOpprettet, HttpStatus.OK)
  }

  @GetMapping("$HENT_ENGANGSBELOPGRUNNLAG/{engangsbelopId}/{grunnlagId}")
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary ="Henter et engangsbelopgrunnlag")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Engangsbelopgrunnlag funnet"),
      ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuelt grunnlag", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "404", description = "Grunnlag ikke funnet", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))])
    ]
  )

  fun hentEngangsbelopGrunnlag(@PathVariable engangsbelopId: Int, @PathVariable grunnlagId: Int): ResponseEntity<EngangsbelopGrunnlagDto> {
    val engangsbelopGrunnlagFunnet = engangsbelopGrunnlagService.hentEngangsbelopGrunnlag(engangsbelopId, grunnlagId)
    LOGGER.info("Følgende engangsbelopgrunnlag ble funnet: $engangsbelopGrunnlagFunnet")
    return ResponseEntity(engangsbelopGrunnlagFunnet, HttpStatus.OK)
  }

  @GetMapping("$HENT_GRUNNLAG_FOR_ENGANGSBELOP/{engangsbelopId}")
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary ="Henter alle grunnlag for et engangsbelop")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Alle engangsbelopgrunnlag funnet"),
      ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuelt grunnlag", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "404", description = "Grunnlag ikke funnet for Engangsbelop", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))])
    ]
  )
  fun hentAlleGrunnlagForEngangsbelop(@PathVariable engangsbelopId: Int): ResponseEntity<List<EngangsbelopGrunnlagDto>> {
    val alleEngangsbelopGrunnlagFunnet = engangsbelopGrunnlagService.hentAlleGrunnlagForEngangsbelop(engangsbelopId)
    LOGGER.info("Følgende engangsbelopgrunnlag ble funnet: $alleEngangsbelopGrunnlagFunnet")
    return ResponseEntity(alleEngangsbelopGrunnlagFunnet, HttpStatus.OK)
  }

  companion object {
    const val OPPRETT_ENGANGSBELOPGRUNNLAG = "/engangsbelopgrunnlag/nytt"
    const val HENT_ENGANGSBELOPGRUNNLAG = "/engangsbelopgrunnlag"
    const val HENT_GRUNNLAG_FOR_ENGANGSBELOP = "/engangsbelopgrunnlag/engangsbelop"
    private val LOGGER = LoggerFactory.getLogger(EngangsbelopGrunnlagController::class.java)
  }
}
