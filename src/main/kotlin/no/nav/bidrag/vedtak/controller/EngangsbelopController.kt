package no.nav.bidrag.vedtak.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.vedtak.ISSUER
import no.nav.bidrag.vedtak.api.engangsbelop.OpprettEngangsbelopRequest
import no.nav.bidrag.vedtak.dto.EngangsbelopDto
import no.nav.bidrag.vedtak.service.EngangsbelopService
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
class EngangsbelopController(private val engangsbelopService: EngangsbelopService) {

    @PostMapping(OPPRETT_ENGANGSBELOP)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Oppretter nytt engangsbelop")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Engangsbelop opprettet"),
            ApiResponse(responseCode = "400", description = "Feil opplysinger oppgitt", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))])
        ]
    )

    fun opprettEngangsbelop(@RequestBody request: OpprettEngangsbelopRequest): ResponseEntity<EngangsbelopDto>? {
        val engangsbelopOpprettet = engangsbelopService.opprettEngangsbelop(request)
        LOGGER.info("Følgende engangsbelop er opprettet: $engangsbelopOpprettet")
        return ResponseEntity(engangsbelopOpprettet, HttpStatus.OK)
    }


    @GetMapping("$HENT_ENGANGSBELOP/{engangsbelopId}")
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter et engangsbeløp")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Engangsbelop funnet"),
            ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuelt engangsbelop", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "404", description = "Engangsbelop ikke funnet", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))])
        ]
    )

    fun hentEngangsbelop(@PathVariable engangsbelopId: Int): ResponseEntity<EngangsbelopDto> {
        val engangsbelopFunnet = engangsbelopService.hentEngangsbelop(engangsbelopId)
        LOGGER.info("Følgende engangsbelop ble funnet: $engangsbelopFunnet")
        return ResponseEntity(engangsbelopFunnet, HttpStatus.OK)
    }

    @GetMapping("$HENT_ENGANGSBELOP_FOR_VEDTAK/{vedtakId}")
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter alle engangsbelop for et vedtak")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Alle engangsbelop funnet"),
            ApiResponse(responseCode = "401", description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "403", description = "Saksbehandler mangler tilgang til å lese data for aktuelt engangsbelop", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "404", description = "Engangsbelop ikke funnet for vedtak", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))])
        ]
    )
    fun hentEngangsbelopForVedtak(@PathVariable vedtakId: Int): ResponseEntity<List<EngangsbelopDto>> {
        val alleEngangsbelopFunnet = engangsbelopService.hentAlleEngangsbelopForVedtak(vedtakId)
        LOGGER.info("Følgende engangsbelop ble funnet: $alleEngangsbelopFunnet")
        return ResponseEntity(alleEngangsbelopFunnet, HttpStatus.OK)
    }

    companion object {
        const val OPPRETT_ENGANGSBELOP = "/engangsbelop/ny"
        const val HENT_ENGANGSBELOP = "/engangsbelop"
        const val HENT_ENGANGSBELOP_FOR_VEDTAK = "/engangsbelop/vedtak"
        private val LOGGER = LoggerFactory.getLogger(EngangsbelopController::class.java)
    }
}