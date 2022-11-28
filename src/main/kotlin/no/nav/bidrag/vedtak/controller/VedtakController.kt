package no.nav.bidrag.vedtak.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettVedtakRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakDto
import no.nav.bidrag.vedtak.ISSUER
import no.nav.bidrag.vedtak.SECURE_LOGGER
import no.nav.bidrag.vedtak.service.VedtakService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.constraints.NotNull

@RestController
@ProtectedWithClaims(issuer = ISSUER)
class VedtakController(private val vedtakService: VedtakService) {

  @PostMapping(OPPRETT_VEDTAK)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Oppretter nytt vedtak")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description =  "Vedtak opprettet"),
      ApiResponse(responseCode = "400", description =  "Feil opplysinger oppgitt", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "401", description =  "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "500", description =  "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "503", description =  "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))])
    ]
  )

  fun opprettVedtak(@Valid @RequestBody request: OpprettVedtakRequestDto): ResponseEntity<Int>? {
    val vedtakOpprettet = vedtakService.opprettVedtak(request)
    LOGGER.info("Vedtak med id $vedtakOpprettet er opprettet")
    return ResponseEntity(vedtakOpprettet, HttpStatus.OK)
  }


  @GetMapping(HENT_VEDTAK)
  @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter et vedtak")
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description =  "Vedtak funnet"),
      ApiResponse(responseCode = "401", description =  "Manglende eller utløpt id-token", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "403", description =  "Saksbehandler mangler tilgang til å lese data for aktuelt vedtak", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "404", description =  "Vedtak ikke funnet", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "500", description =  "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
      ApiResponse(responseCode = "503", description =  "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))])
    ]
  )

  fun hentVedtak(@PathVariable @NotNull vedtakId: Int): ResponseEntity<VedtakDto> {
    val vedtakFunnet = vedtakService.hentVedtak(vedtakId)
    LOGGER.info("Følgende vedtak ble hentet: $vedtakId")
    SECURE_LOGGER.info("Følgende vedtak ble hentet: $vedtakFunnet")
    return ResponseEntity(vedtakFunnet, HttpStatus.OK)
  }

  companion object {
    const val OPPRETT_VEDTAK = "/vedtak/"
    const val HENT_VEDTAK = "/vedtak/{vedtakId}"
    private val LOGGER = LoggerFactory.getLogger(VedtakController::class.java)
  }
}
