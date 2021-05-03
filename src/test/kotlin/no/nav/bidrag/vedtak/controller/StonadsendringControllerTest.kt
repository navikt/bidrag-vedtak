package no.nav.bidrag.vedtak.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.BidragVedtakLocal.Companion.TEST_PROFILE
import no.nav.bidrag.vedtak.api.stonadsendring.OpprettStonadsendringRequest
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.persistence.repository.StonadsendringRepository
import no.nav.bidrag.vedtak.persistence.repository.VedtakRepository
import no.nav.bidrag.vedtak.service.PersistenceService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
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

@DisplayName("StonadsendringControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = WebEnvironment.RANDOM_PORT)
class StonadsendringControllerTest {

  @Autowired
  private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

  @Autowired
  private lateinit var stonadsendringRepository: StonadsendringRepository

  @Autowired
  private lateinit var vedtakRepository: VedtakRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @LocalServerPort
  private val port = 0

  @Value("\${server.servlet.context-path}")
  private val contextPath: String? = null

  private val stonadsendringDtoListe = object : ParameterizedTypeReference<List<StonadsendringDto>>() {}

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal mappe til context path med random port`() {
    assertThat(makeFullContextPath()).isEqualTo("http://localhost:$port/bidrag-vedtak")
  }

  @Test
  fun `skal opprette ny stonadsendring`() {
    // Oppretter ny forekomst av vedtak
    val nyttVedtakOpprettet = persistenceService.opprettVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter ny forekomst av stønadsendring
    val response = securedTestRestTemplate.exchange(
      fullUrlForNyStonadsendring(),
      HttpMethod.POST,
      byggRequest(nyttVedtakOpprettet.vedtakId),
      StonadsendringDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull() },
      Executable { assertThat(response?.body?.stonadType).isEqualTo("BIDRAG") },
      Executable { assertThat(response?.body?.vedtakId).isEqualTo(nyttVedtakOpprettet.vedtakId) },
      Executable { assertThat(response?.body?.behandlingId).isEqualTo("1111") }
    )
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal hente data for en stonadsendring`() {
    // Oppretter ny forekomst av vedtak
    val nyttVedtakOpprettet = persistenceService.opprettVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter ny forekomst av stønadsendring
    val nyStonadsendringOpprettet = persistenceService.opprettStonadsendring(
      StonadsendringDto(
        stonadType = "BIDRAG",
        vedtakId = nyttVedtakOpprettet.vedtakId,
        behandlingId = "1111",
        skyldnerId = "1111",
        kravhaverId = "1111",
        mottakerId = "1111"
      )
    )

    // Henter forekomst
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokStonadsendring()}/${nyStonadsendringOpprettet.stonadsendringId}",
      HttpMethod.GET,
      null,
      StonadsendringDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.stonadsendringId).isEqualTo(nyStonadsendringOpprettet.stonadsendringId) },
      Executable { assertThat(response?.body?.stonadType).isEqualTo(nyStonadsendringOpprettet.stonadType) },
      Executable { assertThat(response?.body?.vedtakId).isEqualTo(nyStonadsendringOpprettet.vedtakId) },
      Executable { assertThat(response?.body?.behandlingId).isEqualTo(nyStonadsendringOpprettet.behandlingId) }
    )
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal hente alle stonadsendringer for et vedtak`() {
    // Oppretter ny forekomst av vedtak
    val nyttVedtakOpprettet1 = persistenceService.opprettVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))
    val nyttVedtakOpprettet2 = persistenceService.opprettVedtak(VedtakDto(17, saksbehandlerId = "TEST", enhetId = "9999"))

    // Oppretter nye forekomster av stønadsendring
    val nyStonadsendringOpprettet1 = persistenceService.opprettStonadsendring(
      StonadsendringDto(
        stonadType = "BIDRAG",
        vedtakId = nyttVedtakOpprettet1.vedtakId,
        behandlingId = "1111",
        skyldnerId = "1111",
        kravhaverId = "1111",
        mottakerId = "1111"
      )
    )

    val nyStonadsendringOpprettet2 = persistenceService.opprettStonadsendring(
      StonadsendringDto(
        stonadType = "BIDRAG",
        vedtakId = nyttVedtakOpprettet1.vedtakId,
        behandlingId = "2222",
        skyldnerId = "2222",
        kravhaverId = "2222",
        mottakerId = "2222"
      )
    )

    // Stonadsendring som ikke skal legges med i resultatet
    persistenceService.opprettStonadsendring(
      StonadsendringDto(
        stonadType = "BIDRAG",
        vedtakId = nyttVedtakOpprettet2.vedtakId,
        behandlingId = "9999",
        skyldnerId = "9999",
        kravhaverId = "9999",
        mottakerId = "9999"
      )
    )

    // Henter forekomster
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokStonadsendringForVedtak()}/${nyttVedtakOpprettet1.vedtakId}",
      HttpMethod.GET,
      null,
      stonadsendringDtoListe
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response.body).isNotNull },
      Executable { assertThat(response.body?.size).isEqualTo(2) },
      Executable { assertThat(response.body?.get(0)?.stonadsendringId).isEqualTo(nyStonadsendringOpprettet1.stonadsendringId) },
      Executable { assertThat(response.body?.get(0)?.stonadType).isEqualTo(nyStonadsendringOpprettet1.stonadType) },
      Executable { assertThat(response.body?.get(0)?.vedtakId).isEqualTo(nyStonadsendringOpprettet1.vedtakId) },
      Executable { assertThat(response.body?.get(0)?.behandlingId).isEqualTo(nyStonadsendringOpprettet1.behandlingId) },
      Executable { assertThat(response.body?.get(1)?.stonadsendringId).isEqualTo(nyStonadsendringOpprettet2.stonadsendringId) },
      Executable { assertThat(response.body?.get(1)?.stonadType).isEqualTo(nyStonadsendringOpprettet2.stonadType) },
      Executable { assertThat(response.body?.get(1)?.vedtakId).isEqualTo(nyStonadsendringOpprettet2.vedtakId) },
      Executable { assertThat(response.body?.get(1)?.behandlingId).isEqualTo(nyStonadsendringOpprettet2.behandlingId) }
    )
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  private fun fullUrlForNyStonadsendring(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + StonadsendringController.OPPRETT_STONADSENDRING).toUriString()
  }

  private fun fullUrlForSokStonadsendring(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + StonadsendringController.HENT_STONADSENDRING).toUriString()
  }

  private fun fullUrlForSokStonadsendringForVedtak(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + StonadsendringController.HENT_STONADSENDRINGER_FOR_VEDTAK).toUriString()
  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port$contextPath"
  }

  private fun byggRequest(vedtakId: Int): HttpEntity<OpprettStonadsendringRequest> {
    return initHttpEntity(OpprettStonadsendringRequest(
      "BIDRAG",
      vedtakId,
      "1111",
      "1111",
      "1111",
      "1111"
    ))
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}