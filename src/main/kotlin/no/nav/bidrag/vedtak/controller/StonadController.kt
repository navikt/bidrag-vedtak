package no.nav.bidrag.vedtak.controller

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import no.nav.bidrag.vedtak.api.AlleStonaderResponse
import no.nav.bidrag.vedtak.api.NyStonadRequest
import no.nav.bidrag.vedtak.dto.StonadDto
import no.nav.bidrag.vedtak.service.StonadService
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
class StonadController(private val stonadService: StonadService) {

  @PostMapping(STONAD_NY)
  @ApiOperation("Opprett ny stønad")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Stønad opprettet"),
      ApiResponse(code = 400, message = "Feil opplysinger oppgitt"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun opprettNyStonad(@RequestBody request: NyStonadRequest): ResponseEntity<StonadDto>? {
    val stonadOpprettet = stonadService.opprettNyStonad(request)
    LOGGER.info("Følgende vedtak er opprettet: $stonadOpprettet")
    return ResponseEntity(stonadOpprettet, HttpStatus.OK)
  }

  @GetMapping("$STONAD_SOK/{stonadId}")
  @ApiOperation("Finn data for en stønad")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Stønad funnet"),
      ApiResponse(code = 401, message = "Manglende eller utløpt id-token"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuelt vedtak"),
      ApiResponse(code = 404, message = "Vedtak ikke funnet"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun finnEnStonad(@PathVariable stonadId: Int): ResponseEntity<StonadDto> {
    val stonadFunnet = stonadService.finnEnStonad(stonadId)
    LOGGER.info("Følgende stønad ble funnet: $stonadFunnet")
    return ResponseEntity(stonadFunnet, HttpStatus.OK)
  }

  @GetMapping(STONAD_SOK)
  @ApiOperation("Finn data for alle stønader")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Alle stønader funnet"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun finnAlleStonader(): ResponseEntity<AlleStonaderResponse> {
    val alleStonader = stonadService.finnAlleStonader()
    LOGGER.info("Alle stønader ble funnet")
    return ResponseEntity(alleStonader, HttpStatus.OK)
  }

  companion object {

    const val STONAD_SOK = "/stonad"
    const val STONAD_NY = "/stonad/ny"
    private val LOGGER = LoggerFactory.getLogger(StonadController::class.java)
  }
}
