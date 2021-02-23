package no.nav.bidrag.vedtak.controller

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import no.nav.bidrag.vedtak.api.AlleVedtakResponse
import no.nav.bidrag.vedtak.api.NyttVedtakRequest
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.service.VedtakService
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
class VedtakController(private val vedtakService: VedtakService) {

  @PostMapping(VEDTAK_NY)
  @ApiOperation("Opprett nytt vedtak")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Vedtak opprettet"),
      ApiResponse(code = 400, message = "Feil opplysinger oppgitt"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun opprettNyttVedtak(@RequestBody request: NyttVedtakRequest): ResponseEntity<VedtakDto>? {
    val vedtakOpprettet = vedtakService.oprettNyttVedtak(request)
    LOGGER.info("Følgende vedtak er opprettet: $vedtakOpprettet")
    return ResponseEntity(vedtakOpprettet, HttpStatus.OK)
  }

  @GetMapping("$VEDTAK_SOK/{vedtakId}")
  @ApiOperation("Finn data for ett vedtak")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Vedtak funnet"),
      ApiResponse(code = 401, message = "Manglende eller utløpt id-token"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuelt vedtak"),
      ApiResponse(code = 404, message = "Vedtak ikke funnet"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun finnEttVedtak(@PathVariable vedtakId: Int): ResponseEntity<VedtakDto> {
    val vedtakFunnet = vedtakService.finnEttVedtak(vedtakId)
    LOGGER.info("Følgende vedtak ble funnet: $vedtakFunnet")
    return ResponseEntity(vedtakFunnet, HttpStatus.OK)
  }

  @GetMapping(VEDTAK_SOK)
  @ApiOperation("Finn data for alle vedtak")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Alle vedtak funnet"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun finnAlleVedtak(): ResponseEntity<AlleVedtakResponse> {
    val alleVedtak = vedtakService.finnAlleVedtak()
    LOGGER.info("Alle vedtak ble funnet")
    return ResponseEntity(alleVedtak, HttpStatus.OK)
  }

  companion object {

    const val VEDTAK_SOK = "/vedtak"
    const val VEDTAK_NY = "/vedtak/ny"
    private val LOGGER = LoggerFactory.getLogger(VedtakController::class.java)
  }
}
