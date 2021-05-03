package no.nav.bidrag.vedtak.controller

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import no.nav.bidrag.vedtak.api.periode.OpprettPeriodeRequest
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

  @PostMapping(OPPRETT_PERIODE)
  @ApiOperation("Oppretter ny periode")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Periode opprettet"),
      ApiResponse(code = 400, message = "Feil opplysinger oppgitt"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )
  fun opprettPeriode(@RequestBody request: OpprettPeriodeRequest): ResponseEntity<PeriodeDto>? {
    val periodeOpprettet = periodeService.opprettPeriode(request)
    LOGGER.info("Følgende periode er opprettet: $periodeOpprettet")
    return ResponseEntity(periodeOpprettet, HttpStatus.OK)
  }

  @GetMapping("$HENT_PERIODE/{periodeId}")
  @ApiOperation("Henter en periode")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Periode funnet"),
      ApiResponse(code = 401, message = "Manglende eller utløpt id-token"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuell periode"),
      ApiResponse(code = 404, message = "Periode ikke funnet"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )
  fun hentPeriode(@PathVariable periodeId: Int): ResponseEntity<PeriodeDto> {
    val periodeFunnet = periodeService.hentPeriode(periodeId)
    LOGGER.info("Følgende periode ble funnet: $periodeFunnet")
    return ResponseEntity(periodeFunnet, HttpStatus.OK)
  }

  @GetMapping("$HENT_PERIODER_FOR_STONADSENDRING/{stonadsendringId}")
  @ApiOperation("Henter alle perioder for en stønadsendring")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Alle perioder funnet"),
      ApiResponse(code = 401, message = "Manglende eller utløpt id-token"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuell periode"),
      ApiResponse(code = 404, message = "Perioder ikke funnet for stønad"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )
  fun hentPerioderForStonadsendring(@PathVariable stonadsendringId: Int): ResponseEntity<List<PeriodeDto>> {
    val allePerioderFunnet = periodeService.hentAllePerioderForStonadsendring(stonadsendringId)
    LOGGER.info("Følgende perioder ble funnet: $allePerioderFunnet")
    return ResponseEntity(allePerioderFunnet, HttpStatus.OK)
  }


  companion object {
    const val OPPRETT_PERIODE = "/periode/ny"
    const val HENT_PERIODE = "/periode"
    const val HENT_PERIODER_FOR_STONADSENDRING = "/periode/stonadsendring"
    private val LOGGER = LoggerFactory.getLogger(PeriodeController::class.java)
  }
}
