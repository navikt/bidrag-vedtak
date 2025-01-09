package no.nav.bidrag.vedtak.controller

import io.mockk.every
import io.mockk.mockkObject
import no.nav.bidrag.commons.service.organisasjon.SaksbehandlernavnProvider
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettVedtakRequestDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.vedtak.BidragVedtakTest
import no.nav.bidrag.vedtak.BidragVedtakTest.Companion.TEST_PROFILE
import no.nav.bidrag.vedtak.TestUtil
import no.nav.bidrag.vedtak.persistence.repository.BehandlingsreferanseRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbeløpGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbeløpRepository
import no.nav.bidrag.vedtak.persistence.repository.GrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeRepository
import no.nav.bidrag.vedtak.persistence.repository.StønadsendringGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.StønadsendringRepository
import no.nav.bidrag.vedtak.persistence.repository.VedtakRepository
import no.nav.bidrag.vedtak.service.PersistenceService
import no.nav.bidrag.vedtak.service.VedtakService
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder

@DisplayName("VedtakControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakTest::class], webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
@AutoConfigureWireMock(port = 0)
class VedtakControllerTest {

    @Autowired
    private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

    @Autowired
    private lateinit var behandlingsreferanseRepository: BehandlingsreferanseRepository

    @Autowired
    private lateinit var engangsbeløpGrunnlagRepository: EngangsbeløpGrunnlagRepository

    @Autowired
    private lateinit var engangsbeløpRepository: EngangsbeløpRepository

    @Autowired
    private lateinit var periodeGrunnlagRepository: PeriodeGrunnlagRepository

    @Autowired
    private lateinit var grunnlagRepository: GrunnlagRepository

    @Autowired
    private lateinit var periodeRepository: PeriodeRepository

    @Autowired
    private lateinit var stønadsendringGrunnlagRepository: StønadsendringGrunnlagRepository

    @Autowired
    private lateinit var stønadsendringRepository: StønadsendringRepository

    @Autowired
    private lateinit var vedtakRepository: VedtakRepository

    @Autowired
    private lateinit var vedtakService: VedtakService

    @Autowired
    private lateinit var persistenceService: PersistenceService

    @LocalServerPort
    private val port = 0

    @BeforeEach
    fun `init`() {
        // Sletter alle forekomster
        behandlingsreferanseRepository.deleteAll()
        engangsbeløpGrunnlagRepository.deleteAll()
        engangsbeløpRepository.deleteAll()
        periodeGrunnlagRepository.deleteAll()
        stønadsendringGrunnlagRepository.deleteAll()
        grunnlagRepository.deleteAll()
        periodeRepository.deleteAll()
        stønadsendringRepository.deleteAll()
        vedtakRepository.deleteAll()
        mockkObject(SaksbehandlernavnProvider)
        every {
            SaksbehandlernavnProvider.hentSaksbehandlernavn(any())
        } returns "Saksbehandler Saksbehandlersen"
    }

    @Test
    fun `skal mappe til context path med random port`() {
        assertThat(makeFullContextPath()).isEqualTo("http://localhost:$port")
    }

    @Test
    fun `skal opprette nytt vedtak`() {
        // Oppretter ny forekomst
        val response = securedTestRestTemplate.exchange(
            fullUrlForNyttVedtak(),
            HttpMethod.POST,
            byggVedtakRequest(),
            String::class.java,
        )

        assertAll(
            Executable { assertThat(response).isNotNull() },
            Executable { assertThat(response.statusCode).isEqualTo(HttpStatus.OK) },
            Executable { assertThat(response.body).isNotNull() },
        )
    }

    @Test
    fun `skal opprette nytt vedtak med input fra fil`() {
        // Bygger request
        val filnavn = "/testfiler/opprett_nytt_vedtak_request_eksempel1.json"
        val request = lesFilOgByggRequest(filnavn)

        // Oppretter ny forekomst
        val opprettResponse = securedTestRestTemplate.exchange(
            fullUrlForNyttVedtak(),
            HttpMethod.POST,
            request,
            String::class.java,
        )

        assertAll(
            Executable { assertThat(opprettResponse).isNotNull() },
            Executable { assertThat(opprettResponse.statusCode).isEqualTo(HttpStatus.OK) },
            Executable { assertThat(opprettResponse.body).isNotNull() },
        )
    }

    @Test
    fun `skal hente alle data for et vedtak`() {
        // Oppretter ny forekomst
        val opprettetVedtakId = vedtakService.opprettVedtak(TestUtil.byggVedtakRequest()).vedtaksid

        // Henter forekomster
        val response = securedTestRestTemplate.getForEntity<VedtakDto>("/vedtak/$opprettetVedtakId")

        assertAll(
            Executable { assertThat(response).isNotNull() },
            Executable { assertThat(response.statusCode).isEqualTo(HttpStatus.OK) },
            Executable { assertThat(response.body).isNotNull() },
        )
    }

    @Test
    fun `skal opprette nytt vedtak med engangsbeløp med input fra fil`() {
        // Bygger request
        val filnavn = "/testfiler/opprett_nytt_vedtak_request_med_engangsbeløp.json"
        val request = lesFilOgByggRequest(filnavn)

        // Oppretter ny forekomst
        val opprettResponse = securedTestRestTemplate.exchange(
            fullUrlForNyttVedtak(),
            HttpMethod.POST,
            request,
            String::class.java,
        )

        assertAll(
            Executable { assertThat(opprettResponse).isNotNull() },
            Executable { assertThat(opprettResponse.statusCode).isEqualTo(HttpStatus.OK) },
            Executable { assertThat(opprettResponse.body).isNotNull() },
        )
    }

    @Test
    fun `skal opprette nytt vedtak og hente det via behandlingsreferanse`() {
        // Oppretter ny forekomst
        val opprettetVedtakId = vedtakService.opprettVedtak(TestUtil.byggVedtakRequest()).vedtaksid
        val vedtak = vedtakService.hentVedtak(opprettetVedtakId)
        val kilde = vedtak.behandlingsreferanseListe[0].kilde
        val behandlingsreferanse = vedtak.behandlingsreferanseListe[0].referanse

        val response = securedTestRestTemplate.getForEntity<List<Int>>("/vedtak/hent-vedtak-for-behandlingsreferanse/$kilde/$behandlingsreferanse")

        assertAll(
            Executable { assertThat(response).isNotNull() },
            Executable { assertThat(response.statusCode).isEqualTo(HttpStatus.OK) },
            Executable { assertThat(response.body).isNotNull() },
        )
    }

    @Test
    fun `skal forsøke å hente ikke-eksisterende vedtak via behandlingsreferanse, respons skal være null`() {
        val kilde = "BISYS_SØKNAD"
        val behandlingsreferanse = "Jeg finnes ikke"

        val response = securedTestRestTemplate.getForEntity<List<Int>>("/vedtak/hent-vedtak-for-behandlingsreferanse/$kilde/$behandlingsreferanse")

        assertAll(
            Executable { assertThat(response).isNotNull() },
            Executable { assertThat(response.statusCode).isEqualTo(HttpStatus.OK) },
            Executable { assertThat(response.body).isEmpty() },
        )
    }

    private fun fullUrlForNyttVedtak(): String = UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + VedtakController.OPPRETT_VEDTAK).toUriString()

    private fun fullUrlForHentVedtak(): String = UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + VedtakController.HENT_VEDTAK).toUriString()

    private fun fullUrlForHentVedtakForBehandlingsreferanse(): String = UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + VedtakController.HENT_VEDTAK_FOR_BEHANDLINGSREFERANSE).toUriString()

    private fun makeFullContextPath(): String = "http://localhost:$port"

    private fun byggVedtakRequest(): HttpEntity<OpprettVedtakRequestDto> = initHttpEntity(TestUtil.byggVedtakRequest())

    // Les inn fil med request-data (json)
    private fun lesFilOgByggRequest(filnavn: String): HttpEntity<String> {
        var json = ""
        try {
            json = this::class.java.getResource(filnavn)!!.readText()
        } catch (e: Exception) {
            Assertions.fail("Klarte ikke å lese fil: $filnavn")
        }
        return initHttpEntity(json)
    }

    private fun <T> initHttpEntity(body: T): HttpEntity<T> {
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(body, httpHeaders)
    }
}
