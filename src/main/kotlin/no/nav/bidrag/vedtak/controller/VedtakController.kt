package no.nav.bidrag.vedtak.controller

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import no.nav.bidrag.vedtak.api.vedtak.HentKomplettVedtakResponse
import no.nav.bidrag.vedtak.api.vedtak.OpprettKomplettVedtakRequest
import no.nav.bidrag.vedtak.api.vedtak.OpprettVedtakRequest
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

  @PostMapping(OPPRETT_VEDTAK)
  @ApiOperation("Oppretter nytt vedtak")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Vedtak opprettet"),
      ApiResponse(code = 400, message = "Feil opplysinger oppgitt"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun opprettVedtak(@RequestBody request: OpprettVedtakRequest): ResponseEntity<VedtakDto>? {
    val vedtakOpprettet = vedtakService.opprettVedtak(request)
    LOGGER.info("Følgende vedtak er opprettet: $vedtakOpprettet")
    return ResponseEntity(vedtakOpprettet, HttpStatus.OK)
  }

  @GetMapping("$HENT_VEDTAK/{vedtakId}")
  @ApiOperation("Henter et vedtak")
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

  fun hentVedtak(@PathVariable vedtakId: Int): ResponseEntity<VedtakDto> {
    val vedtakFunnet = vedtakService.hentVedtak(vedtakId)
    LOGGER.info("Følgende vedtak ble funnet: $vedtakFunnet")
    return ResponseEntity(vedtakFunnet, HttpStatus.OK)
  }

  @GetMapping(HENT_VEDTAK)
  @ApiOperation("Henter alle vedtak")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Alle vedtak funnet"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun hentAlleVedtak(): ResponseEntity<List<VedtakDto>> {
    val alleVedtakFunnet = vedtakService.hentAlleVedtak()
    LOGGER.info("Alle vedtak ble funnet")
    return ResponseEntity(alleVedtakFunnet, HttpStatus.OK)
  }

  @PostMapping(OPPRETT_VEDTAK_KOMPLETT)
  @ApiOperation("Oppretter nytt komplett vedtak")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Komplett vedtak opprettet"),
      ApiResponse(code = 400, message = "Feil opplysinger oppgitt"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun opprettKomplettVedtak(@RequestBody request: OpprettKomplettVedtakRequest): ResponseEntity<Int>? {
    val komplettVedtakOpprettet = vedtakService.opprettKomplettVedtak(request)
    LOGGER.info("Vedtak med id $komplettVedtakOpprettet er opprettet")
    return ResponseEntity(komplettVedtakOpprettet, HttpStatus.OK)
  }

  @GetMapping("$HENT_VEDTAK_KOMPLETT/{vedtakId}")
  @ApiOperation("Finn komplette data for et vedtak")
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
  fun hentKomplettVedtak(@PathVariable vedtakId: Int): ResponseEntity<HentKomplettVedtakResponse> {
    val komplettVedtakFunnet = vedtakService.hentKomplettVedtak(vedtakId)
    LOGGER.info("Følgende vedtak ble funnet: $komplettVedtakFunnet")
    return ResponseEntity(komplettVedtakFunnet, HttpStatus.OK)
  }

  companion object {
    const val OPPRETT_VEDTAK = "/vedtak/ny"
    const val OPPRETT_VEDTAK_KOMPLETT = "/vedtak/ny/komplett"
    const val HENT_VEDTAK = "/vedtak"
    const val HENT_VEDTAK_KOMPLETT = "/vedtak/komplett"
    private val LOGGER = LoggerFactory.getLogger(VedtakController::class.java)
  }
}
