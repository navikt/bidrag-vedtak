package no.nav.bidrag.vedtak.controller

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import no.nav.bidrag.vedtak.api.AlleStonadsendringerResponse
import no.nav.bidrag.vedtak.api.NyStonadsendringRequest
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

  @PostMapping(STONADSENDRING_NY)
  @ApiOperation("Opprett ny stønadsendring")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Stønadsendring opprettet"),
      ApiResponse(code = 400, message = "Feil opplysinger oppgitt"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun opprettNyStonadsendring(@RequestBody request: NyStonadsendringRequest): ResponseEntity<StonadsendringDto>? {
    val stonadsendringOpprettet = stonadsendringService.opprettNyStonadsendring(request)
    LOGGER.info("Følgende stønadsendring er opprettet: $stonadsendringOpprettet")
    return ResponseEntity(stonadsendringOpprettet, HttpStatus.OK)
  }

  @GetMapping("$STONADSENDRING_SOK/{stonadsendringId}")
  @ApiOperation("Finn data for en stønadsendring")
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

  fun finnEnStonadsendring(@PathVariable stonadsendringId: Int): ResponseEntity<StonadsendringDto> {
    val stonadsendringFunnet = stonadsendringService.finnEnStonadsendring(stonadsendringId)
    LOGGER.info("Følgende stønadsendring ble funnet: $stonadsendringFunnet")
    return ResponseEntity(stonadsendringFunnet, HttpStatus.OK)
  }

  @GetMapping(STONADSENDRING_SOK)
  @ApiOperation("Finn data for alle stønadsendringer")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Alle stønadsendringer funnet"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun finnAlleStonadsendringer(): ResponseEntity<AlleStonadsendringerResponse> {
    val alleStonadsendringer = stonadsendringService.finnAlleStonadsendringer()
    LOGGER.info("Alle stønadsendringer ble funnet")
    return ResponseEntity(alleStonadsendringer, HttpStatus.OK)
  }

  companion object {

    const val STONADSENDRING_SOK = "/stonadsendring"
    const val STONADSENDRING_NY = "/stonadsendring/ny"
    private val LOGGER = LoggerFactory.getLogger(StonadsendringController::class.java)
  }
}
