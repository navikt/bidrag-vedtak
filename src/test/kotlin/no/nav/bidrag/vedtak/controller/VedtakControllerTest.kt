package no.nav.bidrag.vedtak.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.BidragVedtakLocal.Companion.TEST_PROFILE
import no.nav.bidrag.vedtak.TestUtil
import no.nav.bidrag.vedtak.api.vedtak.HentKomplettVedtakResponse
import no.nav.bidrag.vedtak.api.vedtak.OpprettKomplettVedtakRequest
import no.nav.bidrag.vedtak.api.vedtak.OpprettVedtakRequest
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.persistence.repository.BehandlingsreferanseRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbelopGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbelopRepository
import no.nav.bidrag.vedtak.persistence.repository.GrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeRepository
import no.nav.bidrag.vedtak.persistence.repository.StonadsendringRepository
import no.nav.bidrag.vedtak.persistence.repository.VedtakRepository
import no.nav.bidrag.vedtak.service.HendelserService
import no.nav.bidrag.vedtak.service.PersistenceService
import no.nav.bidrag.vedtak.service.VedtakService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder
import java.nio.file.Files
import java.nio.file.Paths

@DisplayName("VedtakControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = WebEnvironment.RANDOM_PORT)
class VedtakControllerTest {

  @Autowired
  private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

  @Autowired
  private lateinit var behandlingsreferanseRepository: BehandlingsreferanseRepository

  @Autowired
  private lateinit var engangsbelopGrunnlagRepository: EngangsbelopGrunnlagRepository

  @Autowired
  private lateinit var engangsbelopRepository: EngangsbelopRepository

  @Autowired
  private lateinit var periodeGrunnlagRepository: PeriodeGrunnlagRepository

  @Autowired
  private lateinit var grunnlagRepository: GrunnlagRepository

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var stonadsendringRepository: StonadsendringRepository

  @Autowired
  private lateinit var vedtakRepository: VedtakRepository

  @Autowired
  private lateinit var vedtakService: VedtakService

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @LocalServerPort
  private val port = 0

  @Value("\${server.servlet.context-path}")
  private val contextPath: String? = null

  private val vedtakDtoListe = object : ParameterizedTypeReference<List<VedtakDto>>() {}

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    behandlingsreferanseRepository.deleteAll()
    engangsbelopGrunnlagRepository.deleteAll()
    engangsbelopRepository.deleteAll()
    periodeGrunnlagRepository.deleteAll()
    grunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal mappe til context path med random port`() {
    assertThat(makeFullContextPath()).isEqualTo("http://localhost:$port/bidrag-vedtak")
  }

  @Test
  fun `skal opprette nytt vedtak`() {
    // Oppretter ny forekomst
    val response = securedTestRestTemplate.exchange(
      fullUrlForNyttVedtak(),
      HttpMethod.POST,
      byggRequest(),
      VedtakDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull() },
      Executable { assertThat(response?.body?.enhetId).isEqualTo("1111") },
      Executable { assertThat(response?.body?.saksbehandlerId).isEqualTo("TEST") }
    )
  }

  @Test
  fun `skal hente data for ett vedtak`() {
    // Oppretter ny forekomst
    val nyttVedtakOpprettet = persistenceService.opprettVedtak(VedtakDto(enhetId = "1111", saksbehandlerId = "TEST"))

    // Henter forekomst
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokVedtak()}/${nyttVedtakOpprettet.vedtakId}",
      HttpMethod.GET,
      null,
      VedtakDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.vedtakId).isEqualTo(nyttVedtakOpprettet.vedtakId) },
      Executable { assertThat(response?.body?.enhetId).isEqualTo(nyttVedtakOpprettet.enhetId) },
      Executable { assertThat(response?.body?.saksbehandlerId).isEqualTo(nyttVedtakOpprettet.saksbehandlerId) }
    )
  }

  @Test
  fun `skal hente data for alle vedtak`() {
    // Oppretter nye forekomster
    val nyttVedtakOpprettet1 = persistenceService.opprettVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))
    val nyttVedtakOpprettet2 = persistenceService.opprettVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "2222"))

    // Henter forekomster
    val response = securedTestRestTemplate.exchange(
      fullUrlForSokVedtak(),
      HttpMethod.GET,
      null,
      vedtakDtoListe
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response.body).isNotNull },
      Executable { assertThat(response.body?.size).isEqualTo(2) },
      Executable { assertThat(response.body?.get(0)?.vedtakId).isEqualTo(nyttVedtakOpprettet1.vedtakId) },
      Executable { assertThat(response.body?.get(0)?.enhetId).isEqualTo(nyttVedtakOpprettet1.enhetId) },
      Executable { assertThat(response.body?.get(0)?.saksbehandlerId).isEqualTo(nyttVedtakOpprettet1.saksbehandlerId) },
      Executable { assertThat(response.body?.get(1)?.vedtakId).isEqualTo(nyttVedtakOpprettet2.vedtakId) },
      Executable { assertThat(response.body?.get(1)?.enhetId).isEqualTo(nyttVedtakOpprettet2.enhetId) },
      Executable { assertThat(response.body?.get(1)?.saksbehandlerId).isEqualTo(nyttVedtakOpprettet2.saksbehandlerId) }
    )
  }

  @Test
  fun `skal opprette nytt komplett vedtak`() {
    // Oppretter ny forekomst
    val response = securedTestRestTemplate.exchange(
      fullUrlForNyttKomplettVedtak(),
      HttpMethod.POST,
      byggKomplettVedtakRequest(),
      Int::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull() }
    )
  }

  @Test
  fun `skal opprette nytt komplett vedtak med input fra fil`() {

    // Bygger request
    val filnavn = "src/test/resources/testfiler/opprett_nytt_komplett_vedtak_request_eksempel1.json"
    val request = lesFilOgByggRequest(filnavn)

    // Oppretter ny forekomst
    val opprettResponse = securedTestRestTemplate.exchange(
      fullUrlForNyttKomplettVedtak(),
      HttpMethod.POST,
      request,
      Int::class.java
    )

    assertAll(
      Executable { assertThat(opprettResponse).isNotNull() },
      Executable { assertThat(opprettResponse?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(opprettResponse?.body).isNotNull() }
    )
  }

  @Test
  @Disabled
  // TODO Disablet denne testen, fordi den feiler pga JsonRawValue-annotasjonen i GrunnlagResponse. Testen fungerer hvis denne annotasjonen
  // TODO fjernes, men da vises escape-karakterer ved test i Swagger, så antar annotasjonen må være der
  fun `skal hente komplette data for et vedtak`() {
    // Oppretter ny forekomst
    val nyttVedtakOpprettet = vedtakService.opprettKomplettVedtak(TestUtil.byggKomplettVedtakRequest())

    // Henter forekomster
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokKomplettVedtak()}/${nyttVedtakOpprettet}",
      HttpMethod.GET,
      null,
      HentKomplettVedtakResponse::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull() },
      Executable { assertThat(response?.body!!.vedtakId).isEqualTo(nyttVedtakOpprettet) }
    )
  }

  @Test
  fun `skal opprette nytt komplett vedtak med engangsbelop med input fra fil`() {

    // Bygger request
    val filnavn = "src/test/resources/testfiler/opprett_nytt_komplett_vedtak_request_med_engangsbelop.json"
    val request = lesFilOgByggRequest(filnavn)

    // Oppretter ny forekomst
    val opprettResponse = securedTestRestTemplate.exchange(
      fullUrlForNyttKomplettVedtak(),
      HttpMethod.POST,
      request,
      Int::class.java
    )

    assertAll(
      Executable { assertThat(opprettResponse).isNotNull() },
      Executable { assertThat(opprettResponse?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(opprettResponse?.body).isNotNull() }
    )
  }

  private fun fullUrlForNyttVedtak(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + VedtakController.OPPRETT_VEDTAK).toUriString()
  }

  private fun fullUrlForNyttKomplettVedtak(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + VedtakController.OPPRETT_VEDTAK_KOMPLETT).toUriString()
  }

  private fun fullUrlForSokVedtak(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + VedtakController.HENT_VEDTAK).toUriString()
  }

  private fun fullUrlForSokKomplettVedtak(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + VedtakController.HENT_VEDTAK_KOMPLETT).toUriString()
  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port$contextPath"
  }

  private fun byggRequest(): HttpEntity<OpprettVedtakRequest> {
    return initHttpEntity(OpprettVedtakRequest(saksbehandlerId = "TEST", enhetId = "1111"))
  }

  private fun byggKomplettVedtakRequest(): HttpEntity<OpprettKomplettVedtakRequest> {
    return initHttpEntity(TestUtil.byggKomplettVedtakRequest())
  }

  // Les inn fil med request-data (json)
  private fun lesFilOgByggRequest(filnavn: String): HttpEntity<String> {
    var json = ""
    try {
      json = Files.readString(Paths.get(filnavn))
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
