package no.nav.bidrag.vedtak.controller

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import no.nav.bidrag.vedtak.api.periodegrunnlag.OpprettPeriodeGrunnlagRequest
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

  @PostMapping(OPPRETT_PERIODEGRUNNLAG)
  @ApiOperation("Oppretter nytt periodegrunnlag")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Periodegrunnlag opprettet"),
      ApiResponse(code = 400, message = "Feil opplysinger oppgitt"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )
  fun opprettPeriodeGrunnlag(@RequestBody request: OpprettPeriodeGrunnlagRequest): ResponseEntity<PeriodeGrunnlagDto>? {
    val periodeGrunnlagOpprettet = periodeGrunnlagService.opprettPeriodeGrunnlag(request)
    LOGGER.info("Følgende periodegrunnlag er opprettet: $periodeGrunnlagOpprettet")
    return ResponseEntity(periodeGrunnlagOpprettet, HttpStatus.OK)
  }

  @GetMapping("$HENT_PERIODEGRUNNLAG/{periodeId}/{grunnlagId}")
  @ApiOperation("Henter et periodegrunnlag")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Periodegrunnlag funnet"),
      ApiResponse(code = 401, message = "Manglende eller utløpt id-token"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuelt grunnlag"),
      ApiResponse(code = 404, message = "Grunnlag ikke funnet"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )

  fun hentPeriodeGrunnlag(@PathVariable periodeId: Int, @PathVariable grunnlagId: Int): ResponseEntity<PeriodeGrunnlagDto> {
    val periodeGrunnlagFunnet = periodeGrunnlagService.hentPeriodeGrunnlag(periodeId, grunnlagId)
    LOGGER.info("Følgende periodegrunnlag ble funnet: $periodeGrunnlagFunnet")
    return ResponseEntity(periodeGrunnlagFunnet, HttpStatus.OK)
  }

  @GetMapping("$HENT_PERIODEGRUNNLAG_FOR_PERIODE/{periodeId}")
  @ApiOperation("Henter alle periodegrunnlag for en periode")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "Alle periodegrunnlag funnet"),
      ApiResponse(code = 401, message = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(code = 403, message = "Saksbehandler mangler tilgang til å lese data for aktuelt grunnlag"),
      ApiResponse(code = 404, message = "Grunnlag ikke funnet for vedtak"),
      ApiResponse(code = 500, message = "Serverfeil"),
      ApiResponse(code = 503, message = "Tjeneste utilgjengelig")
    ]
  )
  fun hentAllePeriodeGrunnlagForPeriode(@PathVariable periodeId: Int): ResponseEntity<List<PeriodeGrunnlagDto>> {
    val allePeriodeGrunnlagFunnet = periodeGrunnlagService.hentAllePeriodeGrunnlagForPeriode(periodeId)
    LOGGER.info("Følgende periodegrunnlag ble funnet: $allePeriodeGrunnlagFunnet")
    return ResponseEntity(allePeriodeGrunnlagFunnet, HttpStatus.OK)
  }

  companion object {
    const val OPPRETT_PERIODEGRUNNLAG = "/periodegrunnlag/nytt"
    const val HENT_PERIODEGRUNNLAG = "/periodegrunnlag"
    const val HENT_PERIODEGRUNNLAG_FOR_PERIODE = "/periodegrunnlag/periode"
    private val LOGGER = LoggerFactory.getLogger(PeriodeGrunnlagController::class.java)
  }
}
