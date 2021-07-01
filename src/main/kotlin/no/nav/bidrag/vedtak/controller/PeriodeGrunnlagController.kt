package no.nav.bidrag.vedtak.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.vedtak.ISSUER
import no.nav.bidrag.vedtak.api.periodegrunnlag.OpprettPeriodeGrunnlagRequest
import no.nav.bidrag.vedtak.dto.PeriodeGrunnlagDto
import no.nav.bidrag.vedtak.service.PeriodeGrunnlagService
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
class PeriodeGrunnlagController(private val periodeGrunnlagService: PeriodeGrunnlagService) {

  @PostMapping(OPPRETT_PERIODEGRUNNLAG)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary ="Oppretter nytt periodegrunnlag")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Periodegrunnlag opprettet"),
      ApiResponse(responseCode = "400", description = "Feil opplysinger oppgitt"),
      ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(responseCode = "500", description = "Serverfeil"),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig")
    ]
  )
  fun opprettPeriodeGrunnlag(@RequestBody request: OpprettPeriodeGrunnlagRequest): ResponseEntity<PeriodeGrunnlagDto>? {
    val periodeGrunnlagOpprettet = periodeGrunnlagService.opprettPeriodeGrunnlag(request)
    LOGGER.info("Følgende periodegrunnlag er opprettet: $periodeGrunnlagOpprettet")
    return ResponseEntity(periodeGrunnlagOpprettet, HttpStatus.OK)
  }

  @GetMapping("$HENT_PERIODEGRUNNLAG/{periodeId}/{grunnlagId}")
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary ="Henter et periodegrunnlag")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Periodegrunnlag funnet"),
      ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token"),
      ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuelt grunnlag"),
      ApiResponse(responseCode = "404", description = "Grunnlag ikke funnet"),
      ApiResponse(responseCode = "500", description = "Serverfeil"),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig")
    ]
  )

  fun hentPeriodeGrunnlag(@PathVariable periodeId: Int, @PathVariable grunnlagId: Int): ResponseEntity<PeriodeGrunnlagDto> {
    val periodeGrunnlagFunnet = periodeGrunnlagService.hentPeriodeGrunnlag(periodeId, grunnlagId)
    LOGGER.info("Følgende periodegrunnlag ble funnet: $periodeGrunnlagFunnet")
    return ResponseEntity(periodeGrunnlagFunnet, HttpStatus.OK)
  }

  @GetMapping("$HENT_PERIODEGRUNNLAG_FOR_PERIODE/{periodeId}")
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary ="Henter alle periodegrunnlag for en periode")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Alle periodegrunnlag funnet"),
      ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig"),
      ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuelt grunnlag"),
      ApiResponse(responseCode = "404", description = "Grunnlag ikke funnet for vedtak"),
      ApiResponse(responseCode = "500", description = "Serverfeil"),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig")
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
