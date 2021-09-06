package no.nav.bidrag.vedtak.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.vedtak.ISSUER
import no.nav.bidrag.vedtak.api.grunnlag.OpprettGrunnlagRequest
import no.nav.bidrag.vedtak.dto.GrunnlagDto
import no.nav.bidrag.vedtak.service.GrunnlagService
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
class GrunnlagController(private val grunnlagService: GrunnlagService) {

  @PostMapping(OPPRETT_GRUNNLAG)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary ="Oppretter nytt grunnlag")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Grunnlag opprettet"),
      ApiResponse(responseCode = "400", description = "Feil opplysinger oppgitt", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))])
    ]
  )
  fun opprettGrunnlag(@RequestBody request: OpprettGrunnlagRequest): ResponseEntity<GrunnlagDto>? {
    val grunnlagOpprettet = grunnlagService.opprettGrunnlag(request)
    LOGGER.info("Følgende grunnlag er opprettet: $grunnlagOpprettet")
    return ResponseEntity(grunnlagOpprettet, HttpStatus.OK)
  }

  @GetMapping("$HENT_GRUNNLAG/{grunnlagId}")
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary ="Henter et grunnlag")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Grunnlag funnet"),
      ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuelt grunnlag", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "404", description = "Grunnlag ikke funnet", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))])
    ]
  )

  fun hentGrunnlag(@PathVariable grunnlagId: Int): ResponseEntity<GrunnlagDto> {
    val grunnlagFunnet = grunnlagService.hentGrunnlag(grunnlagId)
    LOGGER.info("Følgende grunnlag ble funnet: $grunnlagFunnet")
    return ResponseEntity(grunnlagFunnet, HttpStatus.OK)
  }

  @GetMapping("$HENT_GRUNNLAG_FOR_VEDTAK/{vedtakId}")
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary ="Henter alle grunnlag for et vedtak")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Alle grunnlag funnet"),
      ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuelt grunnlag", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "404", description = "Grunnlag ikke funnet for vedtak", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))])
    ]
  )
  fun hentGrunnlagForVedtak(@PathVariable vedtakId: Int): ResponseEntity<List<GrunnlagDto>> {
    val alleGrunnlagFunnet = grunnlagService.hentAlleGrunnlagForVedtak(vedtakId)
    LOGGER.info("Følgende grunnlag ble funnet: $alleGrunnlagFunnet")
    return ResponseEntity(alleGrunnlagFunnet, HttpStatus.OK)
  }

  companion object {
    const val OPPRETT_GRUNNLAG = "/grunnlag/ny"
    const val HENT_GRUNNLAG = "/grunnlag"
    const val HENT_GRUNNLAG_FOR_VEDTAK = "/grunnlag/vedtak"
    private val LOGGER = LoggerFactory.getLogger(GrunnlagController::class.java)
  }
}
