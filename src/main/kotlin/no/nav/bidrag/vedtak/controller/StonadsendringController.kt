package no.nav.bidrag.vedtak.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.vedtak.ISSUER
import no.nav.bidrag.vedtak.api.stonadsendring.OpprettStonadsendringRequest
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import no.nav.bidrag.vedtak.service.StonadsendringService
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
class StonadsendringController(private val stonadsendringService: StonadsendringService) {

  @PostMapping(OPPRETT_STONADSENDRING)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary ="Oppretter ny stønadsendring")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Stønadsendring opprettet"),
      ApiResponse(responseCode = "400", description = "Feil opplysinger oppgitt", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))])
    ]
  )

  fun opprettStonadsendring(@RequestBody request: OpprettStonadsendringRequest): ResponseEntity<StonadsendringDto>? {
    val stonadsendringOpprettet = stonadsendringService.opprettStonadsendring(request)
    LOGGER.info("Følgende stønadsendring er opprettet: $stonadsendringOpprettet")
    return ResponseEntity(stonadsendringOpprettet, HttpStatus.OK)
  }

  @GetMapping("$HENT_STONADSENDRING/{stonadsendringId}")
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary ="Henter en stønadsendring")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Stønadsendring funnet"),
      ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuell stønadsendring", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "404", description = "Stønadsendring ikke funnet", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))])
    ]
  )

  fun hentStonadsendring(@PathVariable stonadsendringId: Int): ResponseEntity<StonadsendringDto> {
    val stonadsendringFunnet = stonadsendringService.hentStonadsendring(stonadsendringId)
    LOGGER.info("Følgende stønadsendring ble funnet: $stonadsendringFunnet")
    return ResponseEntity(stonadsendringFunnet, HttpStatus.OK)
  }

  @GetMapping("$HENT_STONADSENDRINGER_FOR_VEDTAK/{vedtakId}")
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary ="Henter alle stønadsendringer for et vedtak")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Alle stønadsendringer funnet"),
      ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuell stønadsendring", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "404", description = "Stonadsendringer ikke funnet for vedtak", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))])
    ]
  )
  fun hentStonadsendringerForVedtak(@PathVariable vedtakId: Int): ResponseEntity<List<StonadsendringDto>> {
    val alleStonadsendringerFunnet = stonadsendringService.hentAlleStonadsendringerForVedtak(vedtakId)
    LOGGER.info("Følgende stønadsendringer ble funnet: $alleStonadsendringerFunnet")
    return ResponseEntity(alleStonadsendringerFunnet, HttpStatus.OK)
  }

  companion object {
    const val OPPRETT_STONADSENDRING = "/stonadsendring/ny"
    const val HENT_STONADSENDRING = "/stonadsendring"
    const val HENT_STONADSENDRINGER_FOR_VEDTAK = "/stonadsendring/vedtak"
    private val LOGGER = LoggerFactory.getLogger(StonadsendringController::class.java)
  }
}