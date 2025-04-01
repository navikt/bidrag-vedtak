package no.nav.bidrag.vedtak.controller

import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import no.nav.bidrag.domene.enums.vedtak.BehandlingsrefKilde
import no.nav.bidrag.transport.behandling.vedtak.request.HentVedtakForStønadRequest
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettVedtakRequestDto
import no.nav.bidrag.transport.behandling.vedtak.response.HentVedtakForStønadResponse
import no.nav.bidrag.transport.behandling.vedtak.response.OpprettVedtakResponseDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.vedtak.SECURE_LOGGER
import no.nav.bidrag.vedtak.service.VedtakService
import no.nav.bidrag.vedtak.util.VedtakUtil.Companion.tilJson
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
@Timed
class VedtakController(private val vedtakService: VedtakService) {

    @PostMapping(OPPRETT_VEDTAK, "/vedtak")
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Oppretter nytt vedtak")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Vedtak opprettet"),
            ApiResponse(responseCode = "400", description = "Feil opplysinger oppgitt", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(
                responseCode = "401",
                description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(responseCode = "409", description = "Unik referanse finnes fra før", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "412", description = "Angitt sisteVedtaksid er ikke nyeste vedtak", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))]),
        ],
    )
    fun opprettVedtak(
        @Valid @RequestBody
        request: OpprettVedtakRequestDto,
    ): ResponseEntity<OpprettVedtakResponseDto>? {
        SECURE_LOGGER.info("Følgende request for å opprette vedtak mottatt: ${tilJson(request)}")
        val vedtakOpprettet = vedtakService.opprettVedtak(
            vedtakRequest = request,
            vedtaksforslag = false,
        )
        LOGGER.info("Vedtak er opprettet med følgende id: ${vedtakOpprettet.vedtaksid}")
        return ResponseEntity(vedtakOpprettet, HttpStatus.OK)
    }

    @GetMapping(HENT_VEDTAK)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter et vedtak")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Vedtak funnet"),
            ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(
                responseCode = "403",
                description = "Saksbehandler mangler tilgang til å lese data for aktuelt vedtak",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(responseCode = "404", description = "Vedtak ikke funnet", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))]),
        ],
    )
    fun hentVedtak(
        @PathVariable @NotNull
        vedtaksid: Int,
    ): ResponseEntity<VedtakDto> {
        LOGGER.info("Request for å hente vedtak med følgende id ble mottatt: $vedtaksid")
        val vedtakFunnet = vedtakService.hentVedtak(vedtaksid)
        SECURE_LOGGER.info("Følgende vedtak ble hentet: $vedtaksid ${tilJson(vedtakFunnet)}")
        return ResponseEntity(vedtakFunnet, HttpStatus.OK)
    }

    @PostMapping(OPPDATER_VEDTAK)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Oppdaterer grunnlag på et eksisterende vedtak")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Vedtak oppdatert"),
            ApiResponse(
                responseCode = "400",
                description = "Data i innsendt vedtak matcher ikke lagrede vedtaksopplysninger",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(responseCode = "404", description = "Vedtak ikke funnet", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))]),
        ],
    )
    fun oppdaterVedtak(
        @PathVariable @NotNull
        vedtaksid: Int,
        @Valid @RequestBody
        request: OpprettVedtakRequestDto,
    ): ResponseEntity<Int>? {
        SECURE_LOGGER.info("Følgende request mottatt om å oppdatere vedtak med id $vedtaksid: ${tilJson(request)}")
        val vedtakOppdatert = vedtakService.oppdaterVedtak(vedtaksid, request)
        LOGGER.info("Vedtak med id $vedtakOppdatert er oppdatert")
        return ResponseEntity(vedtakOppdatert, HttpStatus.OK)
    }

    @PostMapping(HENT_VEDTAK_FOR_SAK)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter endringsvedtak for angitt sak, skyldner, kravhaver og type")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Vedtak hentet"),
            ApiResponse(responseCode = "400", description = "Feil opplysinger oppgitt", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(
                responseCode = "401",
                description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))]),
        ],
    )
    fun hentVedtakForSak(
        @Valid @RequestBody
        request: HentVedtakForStønadRequest,
    ): ResponseEntity<HentVedtakForStønadResponse>? {
        SECURE_LOGGER.info("Følgende request for å hente vedtak for sak ble mottatt: ${tilJson(request)}")
        val respons = vedtakService.hentEndringsvedtakForStønad(request)
        SECURE_LOGGER.info("Følgende endringsvedtak ble hentet for request: ${tilJson(request)}: ${tilJson(respons)}")
        return ResponseEntity(respons, HttpStatus.OK)
    }

    @GetMapping(HENT_VEDTAK_FOR_BEHANDLINGSREFERANSE)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter et vedtak for angitt kilde og behandlingsreferanse")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Vedtak funnet"),
            ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(
                responseCode = "403",
                description = "Saksbehandler mangler tilgang til å lese data for aktuelt vedtak",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(responseCode = "404", description = "Vedtak ikke funnet", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))]),
        ],
    )
    fun hentVedtakForBehandlingsreferanse(
        @PathVariable @NotNull
        kilde: BehandlingsrefKilde,
        @PathVariable @NotNull
        behandlingsreferanse: String,
    ): ResponseEntity<List<Int>> {
        LOGGER.info("Request for å hente vedtak for kilde $kilde og behandlingsreferanse $behandlingsreferanse mottatt")
        val vedtakFunnet = vedtakService.hentVedtakForBehandlingsreferanse(kilde, behandlingsreferanse)
        if (vedtakFunnet.isNotEmpty()) {
            SECURE_LOGGER.info("Følgende vedtak ble hentet: ${tilJson(vedtakFunnet)}")
        } else {
            SECURE_LOGGER.info("Fant ingen vedtak for kilde $kilde og behandlingsreferanse $behandlingsreferanse")
        }
        return ResponseEntity(vedtakFunnet, HttpStatus.OK)
    }

    // Endepunkter for Vedtaksforslag
    @PostMapping(OPPRETT_VEDTAKSFORSLAG, "/vedtaksforslag")
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Oppretter nytt vedtaksforslag")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Vedtaksforslag opprettet"),
            ApiResponse(responseCode = "400", description = "Feil opplysinger oppgitt", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(
                responseCode = "401",
                description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))]),
        ],
    )
    fun opprettVedtaksforslag(
        @Valid @RequestBody
        request: OpprettVedtakRequestDto,
    ): ResponseEntity<Int> {
        SECURE_LOGGER.info("Følgende request for å opprette vedtaksforslag mottatt: ${tilJson(request)}")
        val vedtaksforslagOpprettet = vedtakService.opprettVedtak(
            vedtakRequest = request,
            vedtaksforslag = true,
        )
        LOGGER.info("Vedtaksforslag er opprettet med følgende id: ${vedtaksforslagOpprettet.vedtaksid}")
        return ResponseEntity(vedtaksforslagOpprettet.vedtaksid, HttpStatus.OK)
    }

    @PostMapping(OPPDATER_VEDTAKSFORSLAG)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Oppdaterer grunnlag på et eksisterende vedtaksforslag")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Vedtaksforslag oppdatert"),
            ApiResponse(
                responseCode = "400",
                description = "Data i innsendt vedtak matcher ikke lagrede vedtaksforslagsopplysninger",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Sikkerhetstoken mangler, er utløpt, eller av andre årsaker ugyldig",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(responseCode = "404", description = "Vedtaksforslag ikke funnet", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))]),
        ],
    )
    fun oppdaterVedtaksforslag(
        @PathVariable @NotNull
        vedtaksid: Int,
        @Valid @RequestBody
        request: OpprettVedtakRequestDto,
    ): ResponseEntity<Int>? {
        SECURE_LOGGER.info("Følgende request mottatt om å oppdatere vedtaksforslag med id $vedtaksid: ${tilJson(request)}")
        val vedtaksforslagOppdatert = vedtakService.oppdaterVedtaksforslag(vedtaksid, request)
        LOGGER.info("Vedtaksforslag med id $vedtaksforslagOppdatert er oppdatert")
        return ResponseEntity(vedtaksforslagOppdatert, HttpStatus.OK)
    }

    @DeleteMapping(SLETT_VEDTAKSFORSLAG)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Sletter vedtaksforslag")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "vedtaksforslag slettet"),
            ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(
                responseCode = "403",
                description = "Saksbehandler mangler tilgang til å lese data for aktuelt vedtaksforslag",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(responseCode = "404", description = "Vedtaksforslag ikke funnet", content = [Content(schema = Schema(hidden = true))]),
        ],
    )
    fun slettVedtaksforslag(
        @PathVariable @NotNull
        vedtaksid: Int,
    ): ResponseEntity<Int> {
        LOGGER.info("Request for å slette vedtaksforslag med følgende id ble mottatt: $vedtaksid")
        val vedtaksforslagSlettet = vedtakService.slettVedtaksforslag(vedtaksid)
        SECURE_LOGGER.info("Følgende vedtaksforslag ble slettet: $vedtaksid ${tilJson(vedtaksforslagSlettet)}")
        return ResponseEntity(vedtaksforslagSlettet, HttpStatus.OK)
    }

    @GetMapping(HENT_VEDTAK_FOR_UNIK_REFERANSE)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Henter et vedtak tilknyttet unik referanse")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Vedtak funnet"),
            ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(
                responseCode = "403",
                description = "Saksbehandler mangler tilgang til å lese data for aktuelt vedtak",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(responseCode = "404", description = "Vedtak ikke funnet", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))]),
        ],
    )
    fun hentVedtakForUnikReferanse(
        @PathVariable @NotNull
        unikReferanse: String,
    ): ResponseEntity<VedtakDto> {
        LOGGER.info("Request for å hente vedtak med følgende unike referanse ble mottatt: $unikReferanse")
        val vedtakFunnet = vedtakService.hentVedtakForUnikReferanse(unikReferanse)
        SECURE_LOGGER.info("Følgende vedtak ble hentet: $unikReferanse ${tilJson(vedtakFunnet)}")
        return ResponseEntity(vedtakFunnet, HttpStatus.OK)
    }

    @GetMapping(FATT_VEDTAK_FRA_VEDTAKSFORSLAG)
    @Operation(security = [SecurityRequirement(name = "bearer-key")], summary = "Fatter vedtak fra vedtaksforslag")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Vedtak fattet"),
            ApiResponse(responseCode = "401", description = "Manglende eller utløpt id-token", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(
                responseCode = "403",
                description = "Saksbehandler mangler tilgang til å fatte vedtak",
                content = [Content(schema = Schema(hidden = true))],
            ),
            ApiResponse(responseCode = "404", description = "Vedtaksforslag ikke funnet", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "500", description = "Serverfeil", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "503", description = "Tjeneste utilgjengelig", content = [Content(schema = Schema(hidden = true))]),
        ],
    )
    fun fattVedtakFraVedtaksforslag(
        @PathVariable @NotNull
        vedtaksid: Int,
    ): ResponseEntity<Int> {
        LOGGER.info("Request for å fatte vedtak for vedtaksforslag følgende id ble mottatt: $vedtaksid")
        val vedtakFattet = vedtakService.fattVedtakForVedtaksforslag(vedtaksid)
        SECURE_LOGGER.info("Følgende vedtak ble fattet fra vedtaksforslag: $vedtaksid ${tilJson(vedtakFattet)}")
        return ResponseEntity(vedtakFattet, HttpStatus.OK)
    }

    companion object {
        const val OPPRETT_VEDTAK = "/vedtak/"
        const val HENT_VEDTAK = "/vedtak/{vedtaksid}"
        const val OPPDATER_VEDTAK = "/vedtak/oppdater/{vedtaksid}"
        const val HENT_VEDTAK_FOR_SAK = "/vedtak/hent-vedtak"
        const val HENT_VEDTAK_FOR_BEHANDLINGSREFERANSE = "/vedtak/hent-vedtak-for-behandlingsreferanse/{kilde}/{behandlingsreferanse}"
        const val HENT_VEDTAK_FOR_UNIK_REFERANSE = "/vedtak/hent-vedtak-for-unik-referanse/{unikReferanse}"
        const val OPPRETT_VEDTAKSFORSLAG = "/vedtaksforslag/"
        const val OPPDATER_VEDTAKSFORSLAG = "/vedtaksforslag/oppdater/{vedtaksid}"
        const val SLETT_VEDTAKSFORSLAG = "/vedtaksforslag/slett/{vedtaksid}"
        const val FATT_VEDTAK_FRA_VEDTAKSFORSLAG = "/vedtaksforslag/fatt-vedtak-fra-vedtaksforslag/{vedtaksid}"
        private val LOGGER = LoggerFactory.getLogger(VedtakController::class.java)
    }
}
