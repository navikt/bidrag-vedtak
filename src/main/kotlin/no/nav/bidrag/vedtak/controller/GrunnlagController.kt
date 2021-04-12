package no.nav.bidrag.vedtak.controller

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import no.nav.bidrag.vedtak.api.AlleGrunnlagForVedtakResponse
import no.nav.bidrag.vedtak.dto.GrunnlagDto
import no.nav.bidrag.vedtak.service.GrunnlagService
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
class GrunnlagController(private val grunnlagService: GrunnlagService) {


  @GetMapping("$GRUNNLAG_SOK/{grunnlagId}")
  @ApiOperation("Finn data for et grunnlag")
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

  fun finnGrunnlag(@PathVariable grunnlagId: Int): ResponseEntity<GrunnlagDto> {
    val grunnlagFunnet = grunnlagService.finnGrunnlag(grunnlagId)
    LOGGER.info("Følgende grunnlag ble funnet: $grunnlagFunnet")
    return ResponseEntity(grunnlagFunnet, HttpStatus.OK)
  }

  @GetMapping("$GRUNNLAG_SOK_VEDTAK/{vedtakId}")
  @ApiOperation("finner alle grunnlag for et vedtak")
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
  fun finnGrunnlagForVedtak(@PathVariable vedtakId: Int):
      ResponseEntity<AlleGrunnlagForVedtakResponse> {
    val alleGrunnlagFunnet = grunnlagService.finnAlleGrunnlagForVedtak(vedtakId)
    LOGGER.info("Følgende grunnlag ble funnet: $alleGrunnlagFunnet")
    return ResponseEntity(alleGrunnlagFunnet, HttpStatus.OK)
  }

  companion object {
    const val GRUNNLAG_SOK = "/grunnlag"
    const val GRUNNLAG_SOK_VEDTAK = "/grunnlag/vedtak"
    private val LOGGER = LoggerFactory.getLogger(GrunnlagController::class.java)
  }
}
