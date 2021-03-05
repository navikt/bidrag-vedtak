package no.nav.bidrag.vedtak.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.BidragVedtakLocal.Companion.TEST_PROFILE
import no.nav.bidrag.vedtak.api.AlleStonaderResponse
import no.nav.bidrag.vedtak.api.NyStonadRequest
import no.nav.bidrag.vedtak.dto.StonadDto
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.persistence.repository.StonadRepository
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
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder

@DisplayName("StonadControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = WebEnvironment.RANDOM_PORT)
class StonadControllerTest {

  @Autowired
  private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

  @Autowired
  private lateinit var stonadRepository: StonadRepository

  @Autowired
  private lateinit var vedtakRepository: VedtakRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @LocalServerPort
  private val port = 0

  @Value("\${server.servlet.context-path}")
  private val contextPath: String? = null

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    stonadRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal mappe til context path med random port`() {
    assertThat(makeFullContextPath()).isEqualTo("http://localhost:$port/bidrag-vedtak")
  }

  @Test
  fun `skal opprette ny stonad`() {
    // Oppretter ny forekomst av vedtak
    val nyttVedtakOpprettet = persistenceService.opprettNyttVedtak(VedtakDto(opprettetAv = "TEST", enhetsnummer = "1111"))

    // Oppretter ny forekomst av stønad
    val response = securedTestRestTemplate.exchange(
      fullUrlForNyStonad(),
      HttpMethod.POST,
      byggRequest(nyttVedtakOpprettet.vedtakId),
      StonadDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull() },
      Executable { assertThat(response?.body?.stonadType).isEqualTo("BIDRAG") },
      Executable { assertThat(response?.body?.vedtakId).isEqualTo(nyttVedtakOpprettet.vedtakId) },
      Executable { assertThat(response?.body?.behandlingId).isEqualTo("1111") },
      Executable { assertThat(response?.body?.opprettetAv).isEqualTo("TEST") },
      Executable { assertThat(response?.body?.enhetsnummer).isEqualTo(1111) }
    )
  }

  @Test
  fun `skal finne data for en stonad`() {
    // Oppretter ny forekomst av vedtak
    val nyttVedtakOpprettet = persistenceService.opprettNyttVedtak(VedtakDto(opprettetAv = "TEST", enhetsnummer = "1111"))

    // Oppretter ny forekomst av stønad
    val nyStonadOpprettet = persistenceService.opprettNyStonad(
      StonadDto(
        stonadType = "BIDRAG",
        vedtakId = nyttVedtakOpprettet.vedtakId,
        behandlingId = "1111",
        skyldnerId = "1111",
        kravhaverId = "1111",
        mottakerId = "1111",
        opprettetAv = "TEST",
        enhetsnummer = 1111
      )
    )

    // Henter forekomst
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokStonad()}/${nyStonadOpprettet.stonadId}",
      HttpMethod.GET,
      null,
      StonadDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.stonadId).isEqualTo(nyStonadOpprettet.stonadId) },
      Executable { assertThat(response?.body?.stonadType).isEqualTo(nyStonadOpprettet.stonadType) },
      Executable { assertThat(response?.body?.vedtakId).isEqualTo(nyStonadOpprettet.vedtakId) },
      Executable { assertThat(response?.body?.behandlingId).isEqualTo(nyStonadOpprettet.behandlingId) },
      Executable { assertThat(response?.body?.opprettetAv).isEqualTo(nyStonadOpprettet.opprettetAv) },
      Executable { assertThat(response?.body?.enhetsnummer).isEqualTo(nyStonadOpprettet.enhetsnummer) }
    )
  }

  @Test
  fun `skal finne data for alle stonader`() {
    // Oppretter ny forekomst av vedtak
    val nyttVedtakOpprettet = persistenceService.opprettNyttVedtak(VedtakDto(opprettetAv = "TEST", enhetsnummer = "1111"))

    // Oppretter nye forekomster av stønad
    val nyStonadOpprettet1 = persistenceService.opprettNyStonad(
      StonadDto(
        stonadType = "BIDRAG",
        vedtakId = nyttVedtakOpprettet.vedtakId,
        behandlingId = "1111",
        skyldnerId = "1111",
        kravhaverId = "1111",
        mottakerId = "1111",
        opprettetAv = "TEST",
        enhetsnummer = 1111
      )
    )

    val nyStonadOpprettet2 = persistenceService.opprettNyStonad(
      StonadDto(
        stonadType = "BIDRAG",
        vedtakId = nyttVedtakOpprettet.vedtakId,
        behandlingId = "2222",
        skyldnerId = "2222",
        kravhaverId = "2222",
        mottakerId = "2222",
        opprettetAv = "TEST",
        enhetsnummer = 2222
      )
    )

    // Henter forekomster
    val response = securedTestRestTemplate.exchange(
      fullUrlForSokStonad(),
      HttpMethod.GET,
      null,
      AlleStonaderResponse::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.alleStonader).isNotNull },
      Executable { assertThat(response?.body?.alleStonader!!.size).isEqualTo(2) },
      Executable { assertThat(response?.body?.alleStonader!![0].stonadId).isEqualTo(nyStonadOpprettet1.stonadId) },
      Executable { assertThat(response?.body?.alleStonader!![0].stonadType).isEqualTo(nyStonadOpprettet1.stonadType) },
      Executable { assertThat(response?.body?.alleStonader!![0].vedtakId).isEqualTo(nyStonadOpprettet1.vedtakId) },
      Executable { assertThat(response?.body?.alleStonader!![0].behandlingId).isEqualTo(nyStonadOpprettet1.behandlingId) },
      Executable { assertThat(response?.body?.alleStonader!![0].opprettetAv).isEqualTo(nyStonadOpprettet1.opprettetAv) },
      Executable { assertThat(response?.body?.alleStonader!![0].enhetsnummer).isEqualTo(nyStonadOpprettet1.enhetsnummer) },
      Executable { assertThat(response?.body?.alleStonader!![1].stonadId).isEqualTo(nyStonadOpprettet2.stonadId) },
      Executable { assertThat(response?.body?.alleStonader!![1].stonadType).isEqualTo(nyStonadOpprettet2.stonadType) },
      Executable { assertThat(response?.body?.alleStonader!![1].vedtakId).isEqualTo(nyStonadOpprettet2.vedtakId) },
      Executable { assertThat(response?.body?.alleStonader!![1].behandlingId).isEqualTo(nyStonadOpprettet2.behandlingId) },
      Executable { assertThat(response?.body?.alleStonader!![1].opprettetAv).isEqualTo(nyStonadOpprettet2.opprettetAv) },
      Executable { assertThat(response?.body?.alleStonader!![1].enhetsnummer).isEqualTo(nyStonadOpprettet2.enhetsnummer) },
    )
  }

  private fun fullUrlForNyStonad(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + StonadController.STONAD_NY).toUriString()
  }

  private fun fullUrlForSokStonad(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + StonadController.STONAD_SOK).toUriString()
  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port$contextPath"
  }

  private fun byggRequest(vedtakId: Int): HttpEntity<NyStonadRequest> {
    return initHttpEntity(NyStonadRequest("BIDRAG", vedtakId, "1111", "1111", "1111", "1111", "TEST", 1111))
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}
