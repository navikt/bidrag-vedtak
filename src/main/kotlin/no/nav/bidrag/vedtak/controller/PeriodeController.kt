package no.nav.bidrag.vedtak.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.vedtak.ISSUER
import no.nav.bidrag.vedtak.api.periode.OpprettPeriodeRequest
import no.nav.bidrag.vedtak.dto.PeriodeDto
import no.nav.bidrag.vedtak.service.PeriodeService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = ISSUER)
class PeriodeController(private val periodeService: PeriodeService) {

  @PostMapping(OPPRETT_PERIODE)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary ="Oppretter ny periode")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Periode opprettet"),
      ApiResponse(responseCode = "400", description = "Feil opplysinger oppgitt"),
      ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(responseCode = "500", description = "Serverfeil"),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig")
    ]
  )
  fun opprettPeriode(@RequestBody request: OpprettPeriodeRequest): ResponseEntity<PeriodeDto>? {
    val periodeOpprettet = periodeService.opprettPeriode(request)
    LOGGER.info("Følgende periode er opprettet: $periodeOpprettet")
    return ResponseEntity(periodeOpprettet, HttpStatus.OK)
  }

  @GetMapping("$HENT_PERIODE/{periodeId}")
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary ="Henter en periode")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Periode funnet"),
      ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token"),
      ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuell periode"),
      ApiResponse(responseCode = "404", description = "Periode ikke funnet"),
      ApiResponse(responseCode = "500", description = "Serverfeil"),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig")
    ]
  )
  fun hentPeriode(@PathVariable periodeId: Int): ResponseEntity<PeriodeDto> {
    val periodeFunnet = periodeService.hentPeriode(periodeId)
    LOGGER.info("Følgende periode ble funnet: $periodeFunnet")
    return ResponseEntity(periodeFunnet, HttpStatus.OK)
  }

  @GetMapping("$HENT_PERIODER_FOR_STONADSENDRING/{stonadsendringId}")
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary ="Henter alle perioder for en stønadsendring")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Alle perioder funnet"),
      ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token"),
      ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuell periode"),
      ApiResponse(responseCode = "404", description = "Perioder ikke funnet for stønad"),
      ApiResponse(responseCode = "500", description = "Serverfeil"),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig")
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
