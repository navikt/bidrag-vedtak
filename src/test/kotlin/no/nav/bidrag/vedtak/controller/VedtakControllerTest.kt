package no.nav.bidrag.vedtak.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.BidragVedtakLocal.Companion.TEST_PROFILE
import no.nav.bidrag.vedtak.api.NyttVedtakRequest
import no.nav.bidrag.vedtak.dto.VedtakDto
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

@DisplayName("VedtakControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = WebEnvironment.RANDOM_PORT)
class VedtakControllerTest {

  @Autowired
  private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

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
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.opprettet_av).isEqualTo("TEST") },
      Executable { assertThat(response?.body?.enhetsnummer).isEqualTo("1111") }
    )
  }

  @Test
  fun `skal finne data for et vedtak`() {
    // Oppretter ny forekomst
    val nyttVedtakOpprettet = persistenceService.lagreVedtak(VedtakDto(opprettet_av = "TEST", enhetsnummer = "1111"))

    // Henter forekomst
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokVedtak()}/${nyttVedtakOpprettet.vedtak_id}",
      HttpMethod.GET,
      null,
      VedtakDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.vedtak_id).isEqualTo(nyttVedtakOpprettet.vedtak_id) },
      Executable { assertThat(response?.body?.opprettet_av).isEqualTo(nyttVedtakOpprettet.opprettet_av) },
      Executable { assertThat(response?.body?.enhetsnummer).isEqualTo(nyttVedtakOpprettet.enhetsnummer) }
    )
  }

  private fun fullUrlForNyttVedtak(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + VedtakController.VEDTAK_NY).toUriString()
  }

  private fun fullUrlForSokVedtak(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + VedtakController.VEDTAK_SOK).toUriString()
  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port$contextPath"
  }

  private fun byggRequest(): HttpEntity<NyttVedtakRequest> {
    return initHttpEntity(NyttVedtakRequest("TEST", "1111"))
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}
