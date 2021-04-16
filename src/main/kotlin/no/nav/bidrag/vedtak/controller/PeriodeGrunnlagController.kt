package no.nav.bidrag.vedtak.controller

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import no.nav.bidrag.vedtak.api.AlleGrunnlagForPeriodeResponse
import no.nav.bidrag.vedtak.api.NyttPeriodeGrunnlagRequest
import no.nav.bidrag.vedtak.dto.PeriodeGrunnlagDto
import no.nav.bidrag.vedtak.service.PeriodeGrunnlagService
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
class PeriodeGrunnlagController(private val periodeGrunnlagService: PeriodeGrunnlagService) {

  @PostMapping(PERIODEGRUNNLAG_NYTT)
  @ApiOperation("Opprett nytt periodegrunnlag")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Periodegrunnlag opprettet"),
      ApiResponse(code = 400, message = "Feil opplysinger oppgitt"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )
  fun opprettNyttPeriodeGrunnlag(@RequestBody request: NyttPeriodeGrunnlagRequest): ResponseEntity<PeriodeGrunnlagDto>? {
    val periodeGrunnlagOpprettet = periodeGrunnlagService.opprettNyttPeriodeGrunnlag(request)
    LOGGER.info("Følgende periodegrunnlag er opprettet: $periodeGrunnlagOpprettet")
    periodeGrunnlagService.opprettNyttPeriodeGrunnlag(request)
    return ResponseEntity(periodeGrunnlagOpprettet, HttpStatus.OK)
  }

  @GetMapping("$PERIODEGRUNNLAG_SOK/{periodeId}/{grunnlagId}")
//  @GetMapping("$PERIODEGRUNNLAG_SOK/{input}")
  @ApiOperation("Finn ett grunnlag for en periode")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Grunnlag funnet"),
      ApiResponse(code = 401, message = "Manglende eller utløpt id-token"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuelt grunnlag"),
      ApiResponse(code = 404, message = "Grunnlag ikke funnet"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  // @RequestParam
  fun hentPeriodeGrunnlag(@PathVariable periodeId: Int, @PathVariable grunnlagId: Int): ResponseEntity<PeriodeGrunnlagDto> {
//  fun hentPeriodeGrunnlag(@PathVariable input: String): ResponseEntity<PeriodeGrunnlagDto> {
    val periodeGrunnlagFunnet = periodeGrunnlagService.hentPeriodeGrunnlag(periodeId, grunnlagId)
//    val periodeGrunnlagFunnet = periodeGrunnlagService.hentPeriodeGrunnlag(1, 1)
    LOGGER.info("Følgende grunnlag ble funnet: $periodeGrunnlagFunnet")
    return ResponseEntity(periodeGrunnlagFunnet, HttpStatus.OK)
  }

  @GetMapping("$PERIODEGRUNNLAG_SOK_PERIODE/{periodeId}")
  @ApiOperation("finner alle grunnlag for en periode")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Alle grunnlag funnet"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuelt grunnlag"),
      ApiResponse(code = 404, message = "Grunnlag ikke funnet for vedtak"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )
  fun hentAlleGrunnlagForPeriode(@PathVariable periodeId: Int):
      ResponseEntity<AlleGrunnlagForPeriodeResponse> {
    val alleGrunnlagFunnet = periodeGrunnlagService.hentAlleGrunnlagForPeriode(periodeId)
    LOGGER.info("Følgende grunnlag ble funnet: $alleGrunnlagFunnet")
    return ResponseEntity(alleGrunnlagFunnet, HttpStatus.OK)
  }

  companion object {
    const val PERIODEGRUNNLAG_NYTT = "/periodegrunnlag/nytt"
    const val PERIODEGRUNNLAG_SOK = "/periodegrunnlag"
    const val PERIODEGRUNNLAG_SOK_PERIODE = "/periodegrunnlag/periode"
    private val LOGGER = LoggerFactory.getLogger(PeriodeGrunnlagController::class.java)
  }
}
