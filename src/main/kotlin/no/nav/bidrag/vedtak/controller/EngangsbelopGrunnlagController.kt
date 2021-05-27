package no.nav.bidrag.vedtak.controller

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
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
  @ApiOperation("Oppretter nytt engangsbelopgrunnlag")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Engangsbelopgrunnlag opprettet"),
      ApiResponse(code = 400, message = "Feil opplysinger oppgitt"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )
  fun opprettEngangsbelopGrunnlag(@RequestBody request: OpprettEngangsbelopGrunnlagRequest): ResponseEntity<EngangsbelopGrunnlagDto>? {
    val engangsbelopGrunnlagOpprettet = engangsbelopGrunnlagService.opprettEngangsbelopGrunnlag(request)
    LOGGER.info("Følgende engangsbelopgrunnlag er opprettet: $engangsbelopGrunnlagOpprettet")
    return ResponseEntity(engangsbelopGrunnlagOpprettet, HttpStatus.OK)
  }

  @GetMapping("$HENT_ENGANGSBELOPGRUNNLAG/{engangsbelopId}/{grunnlagId}")
  @ApiOperation("Henter et engangsbelopgrunnlag")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Engangsbelopgrunnlag funnet"),
      ApiResponse(code = 401, message = "Manglende eller utløpt id-token"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuelt grunnlag"),
      ApiResponse(code = 404, message = "Grunnlag ikke funnet"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun hentEngangsbelopGrunnlag(@PathVariable engangsbelopId: Int, @PathVariable grunnlagId: Int): ResponseEntity<EngangsbelopGrunnlagDto> {
    val engangsbelopGrunnlagFunnet = engangsbelopGrunnlagService.hentEngangsbelopGrunnlag(engangsbelopId, grunnlagId)
    LOGGER.info("Følgende engangsbelopgrunnlag ble funnet: $engangsbelopGrunnlagFunnet")
    return ResponseEntity(engangsbelopGrunnlagFunnet, HttpStatus.OK)
  }

  @GetMapping("$HENT_GRUNNLAG_FOR_ENGANGSBELOP/{engangsbelopId}")
  @ApiOperation("Henter alle grunnlag for et engangsbelop")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Alle engangsbelopgrunnlag funnet"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuelt grunnlag"),
      ApiResponse(code = 404, message = "Grunnlag ikke funnet for Engangsbelop"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
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
