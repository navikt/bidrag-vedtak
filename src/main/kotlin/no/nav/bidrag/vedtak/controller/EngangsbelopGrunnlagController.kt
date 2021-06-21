package no.nav.bidrag.vedtak.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.vedtak.api.engangsbelopgrunnlag.OpprettEngangsbelopGrunnlagRequest
import no.nav.bidrag.vedtak.dto.EngangsbelopGrunnlagDto
import no.nav.bidrag.vedtak.service.EngangsbelopGrunnlagService
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
class EngangsbelopGrunnlagController(private val engangsbelopGrunnlagService: EngangsbelopGrunnlagService) {

  @PostMapping(OPPRETT_ENGANGSBELOPGRUNNLAG)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary ="Oppretter nytt engangsbelopgrunnlag")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Engangsbelopgrunnlag opprettet"),
      ApiResponse(responseCode = "400", description = "Feil opplysinger oppgitt"),
      ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(responseCode = "500", description = "Serverfeil"),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig")
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
      ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token"),
      ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuelt grunnlag"),
      ApiResponse(responseCode = "404", description = "Grunnlag ikke funnet"),
      ApiResponse(responseCode = "500", description = "Serverfeil"),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig")
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
      ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuelt grunnlag"),
      ApiResponse(responseCode = "404", description = "Grunnlag ikke funnet for Engangsbelop"),
      ApiResponse(responseCode = "500", description = "Serverfeil"),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig")
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
