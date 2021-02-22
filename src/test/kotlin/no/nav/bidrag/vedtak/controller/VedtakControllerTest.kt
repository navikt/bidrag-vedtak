package no.nav.bidrag.vedtak.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.BidragVedtakLocal.Companion.TEST_PROFILE
import no.nav.bidrag.vedtak.api.OppretteNyttVedtakRequest
import no.nav.bidrag.vedtak.persistence.repository.VedtakRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
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

  @LocalServerPort
  private val port = 0

  @Value("\${server.servlet.context-path}")
  private val contextPath: String? = null

  @Test
  fun `skal mappe til context path med random port`() {
    assertThat(makeFullContextPath()).isEqualTo("http://localhost:$port/bidrag-vedtak")
  }

  @Test
  fun `skal opprette nytt vedtak dummy`() {
    val response = securedTestRestTemplate?.exchange(
      fullUrlForNyttVedtakDummy(),
      HttpMethod.POST,
      null,
      String::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.CREATED) },
      Executable { assertThat(response?.body).isEqualTo("Nytt vedtak opprettet") },
    )
  }

  @Test
  fun `skal ha riktig context-path`() {
    assertThat(fullUrlForNyttVedtak()).isEqualTo("${makeFullContextPath()}/vedtak/ny")
  }

  @Test
  fun `skal opprette nytt vedtak`() {

    vedtakRepository.deleteAll()

    val response = securedTestRestTemplate.exchange(
      fullUrlForNyttVedtak(),
      HttpMethod.POST,
      byggRequest(),
      String::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).contains("Nytt vedtak opprettet") }
    )

    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal finne data for et vedtak dummy`() {
    val response = securedTestRestTemplate?.exchange(
      fullUrlForSokVedtakDummy() + "/1",
      HttpMethod.GET,
      null,
      String::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isEqualTo("Vedtak med vedtaksnummer 1 funnet") },
    )
  }

  private fun fullUrlForNyttVedtakDummy(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + VedtakController.VEDTAK_NY_DUMMY).toUriString()
  }

  private fun fullUrlForNyttVedtak(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + VedtakController.VEDTAK_NY).toUriString()
  }

  private fun fullUrlForSokVedtakDummy(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + VedtakController.VEDTAK_SOK_DUMMY).toUriString()
  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port$contextPath"
  }

  private fun byggRequest(): HttpEntity<OppretteNyttVedtakRequest> {
    return initHttpEntity(OppretteNyttVedtakRequest("TEST", "1111"))
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}
