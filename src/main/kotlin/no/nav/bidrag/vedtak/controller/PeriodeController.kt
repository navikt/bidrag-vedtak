package no.nav.bidrag.vedtak.controller

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import no.nav.bidrag.vedtak.api.AllePerioderForStonadResponse
import no.nav.bidrag.vedtak.api.NyPeriodeRequest
import no.nav.bidrag.vedtak.dto.PeriodeDto
import no.nav.bidrag.vedtak.service.PeriodeService
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
class PeriodeController(private val periodeService: PeriodeService) {

  @PostMapping(PERIODE_NY)
  @ApiOperation("Opprette ny periode")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Periode opprettet"),
      ApiResponse(code = 400, message = "Feil opplysinger oppgitt"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )
  fun opprettNyPeriode(@RequestBody request: NyPeriodeRequest): ResponseEntity<PeriodeDto>? {
    val periodeOpprettet = periodeService.opprettNyPeriode(request)
    LOGGER.info("Følgende periode er opprettet: $periodeOpprettet")
    periodeService.opprettNyPeriode(request)
    return ResponseEntity(periodeOpprettet, HttpStatus.OK)
  }

  @GetMapping("$PERIODE_SOK/{periodeId}")
  @ApiOperation("Finn data for en periode")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Data for periode hentet"),
      ApiResponse(code = 401, message = "Manglende eller utløpt id-token"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuell periode"),
      ApiResponse(code = 404, message = "Periode ikke funnet"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )
  fun finnPeriode(@PathVariable periodeId: Int): ResponseEntity<PeriodeDto> {
    val periodeFunnet = periodeService.finnPeriode(periodeId)
    LOGGER.info("Følgende periode ble funnet: $periodeFunnet")
    return ResponseEntity(periodeFunnet, HttpStatus.OK)
  }

  @GetMapping("$PERIODE_SOK_STONADSENDRING/{stonadsendringIdListe}")
  @ApiOperation("Finn alle perioder for en stønad")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Data for periode hentet"),
      ApiResponse(code = 401, message = "Manglende eller utløpt id-token"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuell periode"),
      ApiResponse(code = 404, message = "Perioder ikke funnet for stønad"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )
  fun finnAllePerioderForStonad(@PathVariable stonadsendringIdListe: List<Int>): ResponseEntity<AllePerioderForStonadResponse> {
    val allePerioderFunnet = periodeService.finnAllePerioderForStonad(stonadsendringIdListe)
    LOGGER.info("Følgende perioder ble funnet: $allePerioderFunnet")
    return ResponseEntity(allePerioderFunnet, HttpStatus.OK)
  }


  companion object {
    const val PERIODE_SOK = "/periode"
    const val PERIODE_SOK_STONADSENDRING = "/periode/stonad"
    const val PERIODE_NY = "/periode/ny"
    private val LOGGER = LoggerFactory.getLogger(PeriodeController::class.java)
  }
}
