package no.nav.bidrag.periode.controller

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import no.nav.bidrag.vedtak.api.OppretteNyPeriodeRequest
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

  @GetMapping("$PERIODE_SOK_DUMMY/{periodeId}")
  @ApiOperation(value = "Dummy finn data for en periode")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Data for periode hentet"),
      ApiResponse(code = 401, message = "Manglende eller utløpt id-token"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuell periode"),
      ApiResponse(code = 404, message = "Periode ikke funnet")
    ]
  )
  fun finnPeriodeDummy(@PathVariable periodeId: String?): ResponseEntity<String> {
//  public ResponseEntity<PeriodeDto> finnPeriode(@PathVariable String periodesnummer) {

//    Kaster exception med HttpStatus.FORBIDDEN (403) hvis tilgangskontroll feiler.
//    accessControlService.sjekkTilgangPeriode(periodesnummer);
    val muligPeriode = periodeService.finnPeriodeDummy(periodeId!!)
    return ResponseEntity("Periode med periodeid $muligPeriode funnet", HttpStatus.OK)

//    return muligPeriode
//        .map(PeriodeDto -> new ResponseEntity<>(periodeDto, HttpStatus.OK))
//        .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
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
    val periodeInformasjon = periodeService.finnPeriode(periodeId)
    return ResponseEntity(periodeInformasjon, HttpStatus.OK)
  }

  @PostMapping(PERIODE_NY_DUMMY)
  @ApiOperation(value = "Dummy opprette nytt periode")
  fun nyPeriodeDummy(): ResponseEntity<String> {
//  public ResponseEntity<NyttPeriodeResponseDto> post(
//      @ApiParam(name = "X-Enhet", required = true, value = "Saksbehandlers påloggede enhet") @RequestHeader("X-Enhet") String enhet,
//      NyttPeriodeCommandDto nyttPeriodeCommandDto) {
//    LOGGER.info("Oppretter nytt periode. Saksbehandlers påloggede enhet: {}", enhet);
//    var nyttPeriodeResponseDto = periodeService.nyttPeriode();
    periodeService.nyPeriodeDummy()
    return ResponseEntity("Ny periode opprettet", HttpStatus.CREATED)
    //    return new ResponseEntity<>(nyttPeriodeResponseDto, HttpStatus.CREATED);
  }

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
  fun nyPeriode(@RequestBody request: OppretteNyPeriodeRequest): ResponseEntity<String>? {
    periodeService.opprettNyPeriode(request)
    return ResponseEntity("Ny periode opprettet", HttpStatus.OK)
  }

  companion object {
    const val PERIODE_SOK_DUMMY = "/periode/dummy"
    const val PERIODE_SOK = "/periode"
    const val PERIODE_NY_DUMMY = "/periode/ny/dummy"
    const val PERIODE_NY = "/periode/ny"
    private val LOGGER = LoggerFactory.getLogger(PeriodeController::class.java)
  }
}
