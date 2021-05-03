package no.nav.bidrag.vedtak.controller

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import no.nav.bidrag.vedtak.api.stonadsendring.OpprettStonadsendringRequest
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import no.nav.bidrag.vedtak.service.StonadsendringService
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
class StonadsendringController(private val stonadsendringService: StonadsendringService) {

  @PostMapping(OPPRETT_STONADSENDRING)
  @ApiOperation("Oppretter ny stønadsendring")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Stønadsendring opprettet"),
      ApiResponse(code = 400, message = "Feil opplysinger oppgitt"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun opprettStonadsendring(@RequestBody request: OpprettStonadsendringRequest): ResponseEntity<StonadsendringDto>? {
    val stonadsendringOpprettet = stonadsendringService.opprettStonadsendring(request)
    LOGGER.info("Følgende stønadsendring er opprettet: $stonadsendringOpprettet")
    return ResponseEntity(stonadsendringOpprettet, HttpStatus.OK)
  }

  @GetMapping("$HENT_STONADSENDRING/{stonadsendringId}")
  @ApiOperation("Henter en stønadsendring")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Stønadsendring funnet"),
      ApiResponse(code = 401, message = "Manglende eller utløpt id-token"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuell stønadsendring"),
      ApiResponse(code = 404, message = "Stønadsendring ikke funnet"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun hentStonadsendring(@PathVariable stonadsendringId: Int): ResponseEntity<StonadsendringDto> {
    val stonadsendringFunnet = stonadsendringService.hentStonadsendring(stonadsendringId)
    LOGGER.info("Følgende stønadsendring ble funnet: $stonadsendringFunnet")
    return ResponseEntity(stonadsendringFunnet, HttpStatus.OK)
  }

  @GetMapping("$HENT_STONADSENDRINGER_FOR_VEDTAK/{vedtakId}")
  @ApiOperation("Henter alle stønadsendringer for et vedtak")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Alle stønadsendringer funnet"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuell stønadsendring"),
      ApiResponse(code = 404, message = "Stonadsendringer ikke funnet for vedtak"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )
  fun hentStonadsendringerForVedtak(@PathVariable vedtakId: Int): ResponseEntity<List<StonadsendringDto>> {
    val alleStonadsendringerFunnet = stonadsendringService.hentAlleStonadsendringerForVedtak(vedtakId)
    LOGGER.info("Følgende stønadsendringer ble funnet: $alleStonadsendringerFunnet")
    return ResponseEntity(alleStonadsendringerFunnet, HttpStatus.OK)
  }

  companion object {
    const val OPPRETT_STONADSENDRING = "/stonadsendring/ny"
    const val HENT_STONADSENDRING = "/stonadsendring"
    const val HENT_STONADSENDRINGER_FOR_VEDTAK = "/stonadsendring/vedtak"
    private val LOGGER = LoggerFactory.getLogger(StonadsendringController::class.java)
  }
}