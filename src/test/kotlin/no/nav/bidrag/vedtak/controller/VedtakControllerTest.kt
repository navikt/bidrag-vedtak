package no.nav.bidrag.vedtak.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.BidragVedtakLocal.Companion.TEST_PROFILE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles


@DisplayName("VedtakControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = WebEnvironment.RANDOM_PORT)
class VedtakControllerTest {

  @Autowired
  private val securedTestRestTemplate: HttpHeaderTestRestTemplate? = null

  @LocalServerPort
  private val port = 0

  @Value("\${server.servlet.context-path}")
  private val contextPath: String? = null

  @DisplayName("Skal mappe til context path med random port")
  @Test
  fun skalMappeTilContextPath() {
    assertThat(makeFullContextPath()).isEqualTo("http://localhost:$port/bidrag-vedtak")
  }

//  @Test
//  @DisplayName("Skal opprette nytt vedtak")
//  fun skalOppretteNyttVedtak() {
//    val response = securedTestRestTemplate?.exchange(
//      fullUrlForNyttVedtak(),
//      HttpMethod.POST,
//      null,
//      String::class.java
//    )
//
//    assertAll(
//      Executable { assertThat(response).isNotNull() },
//      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.CREATED) },
//      Executable { assertThat(response?.body).isEqualTo("Nytt vedtak opprettet") },
//    )
//  }
//
//  @Test
//  @DisplayName("Skal finne data for et vedtak")
//  fun skalFinneDataForVedtak() {
//    val response = securedTestRestTemplate?.exchange(
//      fullUrlForSokVedtak() + "/1",
//      HttpMethod.GET,
//      null,
//      String::class.java
//    )
//
//    assertAll(
//      Executable { assertThat(response).isNotNull() },
//      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
//      Executable { assertThat(response?.body).isEqualTo("Vedtak med vedtaksnummer 1 funnet") },
//    )
//  }
//
//  private fun fullUrlForNyttVedtak(): String? {
//    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + VedtakController.VEDTAK_NY).toUriString()
//  }
//
//  private fun fullUrlForSokVedtak(): String? {
//    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + VedtakController.VEDTAK_SOK).toUriString()
//  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port$contextPath"
  }
}