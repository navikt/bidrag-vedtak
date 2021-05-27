package no.nav.bidrag.vedtak.controller

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import no.nav.bidrag.vedtak.dto.EngangsbelopDto
import no.nav.bidrag.vedtak.service.EngangsbelopService
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
class EngangsbelopController(private val engangsbelopService: EngangsbelopService) {

  @GetMapping("$HENT_ENGANGSBELOP/{engangsbelopId}")
  @ApiOperation("Henter et engangsbeløp")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Engangsbelop funnet"),
      ApiResponse(code = 401, message = "Manglende eller utløpt id-token"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuelt engangsbelop"),
      ApiResponse(code = 404, message = "Engangsbelop ikke funnet"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun hentEngangsbelop(@PathVariable engangsbelopId: Int): ResponseEntity<EngangsbelopDto> {
    val engangsbelopFunnet = engangsbelopService.hentEngangsbelop(engangsbelopId)
    LOGGER.info("Følgende engangsbelop ble funnet: $engangsbelopFunnet")
    return ResponseEntity(engangsbelopFunnet, HttpStatus.OK)
  }

  @GetMapping("$HENT_ENGANGSBELOP_FOR_VEDTAK/{vedtakId}")
  @ApiOperation("Henter alle stønadsendringer for et vedtak")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Alle engangsbelop funnet"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuelt engangsbelop"),
      ApiResponse(code = 404, message = "Engangsbelop ikke funnet for vedtak"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )
  fun hentStonadsendringerForVedtak(@PathVariable vedtakId: Int): ResponseEntity<List<EngangsbelopDto>> {
    val alleEngangsbelopFunnet = engangsbelopService.hentAlleEngangsbelopForVedtak(vedtakId)
    LOGGER.info("Følgende engangsbelop ble funnet: $alleEngangsbelopFunnet")
    return ResponseEntity(alleEngangsbelopFunnet, HttpStatus.OK)
  }

  companion object {
    const val HENT_ENGANGSBELOP = "/engangsbelop"
    const val HENT_ENGANGSBELOP_FOR_VEDTAK = "/engangsbelop/vedtak"
    private val LOGGER = LoggerFactory.getLogger(EngangsbelopController::class.java)
  }
}