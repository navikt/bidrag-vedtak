package no.nav.bidrag.vedtak.controller

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import no.nav.bidrag.vedtak.service.VedtakService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController //@Protected
class VedtakController(  //  private final AccessControlService accessControlService;
  private val vedtakService: VedtakService
) {

  @GetMapping(VEDTAK_SOK + "/{vedtaksnummer}")
  @ApiOperation(value = "Finn data for et vedtak")
  @ApiResponses(value = [
      ApiResponse(code = 200, message = "Data for vedtak hentet"),
      ApiResponse(code = 401, message = "Manglende eller utløpt id-token"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuelt vedtak"),
      ApiResponse(code = 404, message = "Vedtak ikke funnet")
    ]
  )
  fun find(@PathVariable vedtaksnummer: String?): ResponseEntity<String> {
//  public ResponseEntity<VedtakDto> finnVedtak(@PathVariable String vedtaksnummer) {

    // Kaster exception med HttpStatus.FORBIDDEN (403) hvis tilgangskontroll feiler.
//    accessControlService.sjekkTilgangVedtak(vedtaksnummer);
    val muligVedtak = vedtakService.finnVedtak(vedtaksnummer!!)
    return ResponseEntity("Vedtak med vedtaksnummer $muligVedtak funnet", HttpStatus.OK)

//    return muligVedtak
//        .map(VedtakDto -> new ResponseEntity<>(vedtakDto, HttpStatus.OK))
//        .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @PostMapping(VEDTAK_NY)
  @ApiOperation(value = "Opprette nytt vedtak")
  fun post(): ResponseEntity<String> {
//  public ResponseEntity<NyttVedtakResponseDto> post(
//      @ApiParam(name = "X-Enhet", required = true, value = "Saksbehandlers påloggede enhet") @RequestHeader("X-Enhet") String enhet,
//      NyttVedtakCommandDto nyttVedtakCommandDto) {
//    LOGGER.info("Oppretter nytt vedtak. Saksbehandlers påloggede enhet: {}", enhet);
//    var nyttVedtakResponseDto = vedtakService.nyttVedtak();
    vedtakService.nyttVedtak()
    return ResponseEntity("Nytt vedtak opprettet", HttpStatus.CREATED)
    //    return new ResponseEntity<>(nyttVedtakResponseDto, HttpStatus.CREATED);
  }

  companion object {
    const val VEDTAK_SOK = "/vedtak"
    const val VEDTAK_NY = "/vedtak/ny"
    private val LOGGER = LoggerFactory.getLogger(VedtakController::class.java)
  }
}