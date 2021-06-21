package no.nav.bidrag.vedtak.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
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
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Oppretter nytt vedtak")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description =  "Vedtak opprettet"),
      ApiResponse(responseCode = "400", description =  "Feil opplysinger oppgitt"),
      ApiResponse(responseCode = "401", description =  "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(responseCode = "500", description =  "Serverfeil"),
      ApiResponse(responseCode = "503", description =  "Tjeneste utilgjengelig")
    ]
  )

  fun opprettVedtak(@RequestBody request: OpprettVedtakRequest): ResponseEntity<VedtakDto>? {
    val vedtakOpprettet = vedtakService.opprettVedtak(request)
    LOGGER.info("Følgende vedtak er opprettet: $vedtakOpprettet")
    return ResponseEntity(vedtakOpprettet, HttpStatus.OK)
  }

  @GetMapping("$HENT_VEDTAK/{vedtakId}")
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter et vedtak")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description =  "Vedtak funnet"),
      ApiResponse(responseCode = "401", description =  "Manglende eller utløpt id-token"),
      ApiResponse(responseCode = "403", description =  "Saksbehandler mangler tilgang til å lese data for aktuelt vedtak"),
      ApiResponse(responseCode = "404", description =  "Vedtak ikke funnet"),
      ApiResponse(responseCode = "500", description =  "Serverfeil"),
      ApiResponse(responseCode = "503", description =  "Tjeneste utilgjengelig")
    ]
  )

  fun hentVedtak(@PathVariable vedtakId: Int): ResponseEntity<VedtakDto> {
    val vedtakFunnet = vedtakService.hentVedtak(vedtakId)
    LOGGER.info("Følgende vedtak ble funnet: $vedtakFunnet")
    return ResponseEntity(vedtakFunnet, HttpStatus.OK)
  }

  @GetMapping(HENT_VEDTAK)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter alle vedtak")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description =  "Alle vedtak funnet"),
      ApiResponse(responseCode = "401", description =  "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(responseCode = "500", description =  "Serverfeil"),
      ApiResponse(responseCode = "503", description =  "Tjeneste utilgjengelig")
    ]
  )

  fun hentAlleVedtak(): ResponseEntity<List<VedtakDto>> {
    val alleVedtakFunnet = vedtakService.hentAlleVedtak()
    LOGGER.info("Alle vedtak ble funnet")
    return ResponseEntity(alleVedtakFunnet, HttpStatus.OK)
  }

  @PostMapping(OPPRETT_VEDTAK_KOMPLETT)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Oppretter nytt komplett vedtak")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description =  "Komplett vedtak opprettet"),
      ApiResponse(responseCode = "400", description =  "Feil opplysinger oppgitt"),
      ApiResponse(responseCode = "401", description =  "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(responseCode = "500", description =  "Serverfeil"),
      ApiResponse(responseCode = "503", description =  "Tjeneste utilgjengelig")
    ]
  )

  fun opprettKomplettVedtak(@RequestBody request: OpprettKomplettVedtakRequest): ResponseEntity<Int>? {
    val komplettVedtakOpprettet = vedtakService.opprettKomplettVedtak(request)
    LOGGER.info("Vedtak med id $komplettVedtakOpprettet er opprettet")
    return ResponseEntity(komplettVedtakOpprettet, HttpStatus.OK)
  }

  @GetMapping("$HENT_VEDTAK_KOMPLETT/{vedtakId}")
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter et komplett vedtak")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description =  "Vedtak funnet"),
      ApiResponse(responseCode = "401", description =  "Manglende eller utløpt id-token"),
      ApiResponse(responseCode = "403", description =  "Saksbehandler mangler tilgang til å lese data for aktuelt vedtak"),
      ApiResponse(responseCode = "404", description =  "Vedtak ikke funnet"),
      ApiResponse(responseCode = "500", description =  "Serverfeil"),
      ApiResponse(responseCode = "503", description =  "Tjeneste utilgjengelig")
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
