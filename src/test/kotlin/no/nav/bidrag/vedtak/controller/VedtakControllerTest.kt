package no.nav.bidrag.vedtak.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.BidragVedtakLocal.Companion.TEST_PROFILE
import no.nav.bidrag.vedtak.TestUtil
import no.nav.bidrag.vedtak.api.AlleVedtakResponse
import no.nav.bidrag.vedtak.api.NyttVedtakRequest
import no.nav.bidrag.vedtak.api.NyttKomplettVedtakRequest
import no.nav.bidrag.vedtak.api.NyttVedtakResponse
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.persistence.repository.GrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeRepository
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
  private lateinit var persistenceService: PersistenceService

  @LocalServerPort
  private val port = 0

  @Value("\${server.servlet.context-path}")
  private val contextPath: String? = null

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
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
  fun `skal finne data for ett vedtak`() {
    // Oppretter ny forekomst
    val nyttVedtakOpprettet = persistenceService.opprettNyttVedtak(VedtakDto(enhetId = "1111", saksbehandlerId = "TEST"))

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
  fun `skal finne data for alle vedtak`() {
    // Oppretter nye forekomster
    val nyttVedtakOpprettet1 = persistenceService.opprettNyttVedtak(VedtakDto(enhetId = "1111", saksbehandlerId = "TEST"))
    val nyttVedtakOpprettet2 = persistenceService.opprettNyttVedtak(VedtakDto(enhetId = "2222", saksbehandlerId = "TEST"))

    // Henter forekomster
    val response = securedTestRestTemplate.exchange(
      fullUrlForSokVedtak(),
      HttpMethod.GET,
      null,
      AlleVedtakResponse::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.alleVedtak).isNotNull },
      Executable { assertThat(response?.body?.alleVedtak!!.size).isEqualTo(2) },
      Executable { assertThat(response?.body?.alleVedtak!![0].vedtakId).isEqualTo(nyttVedtakOpprettet1.vedtakId) },
      Executable { assertThat(response?.body?.alleVedtak!![0].enhetId).isEqualTo(nyttVedtakOpprettet1.enhetId) },
      Executable { assertThat(response?.body?.alleVedtak!![0].saksbehandlerId).isEqualTo(nyttVedtakOpprettet1.saksbehandlerId) },
      Executable { assertThat(response?.body?.alleVedtak!![1].vedtakId).isEqualTo(nyttVedtakOpprettet2.vedtakId) },
      Executable { assertThat(response?.body?.alleVedtak!![1].enhetId).isEqualTo(nyttVedtakOpprettet2.enhetId) },
      Executable { assertThat(response?.body?.alleVedtak!![1].saksbehandlerId).isEqualTo(nyttVedtakOpprettet2.saksbehandlerId) }
    )
  }

  @Test
  fun `skal opprette nytt komplett vedtak`() {
    // Oppretter ny forekomst
    val response = securedTestRestTemplate.exchange(
      fullUrlForNyttKomplettVedtak(),
      HttpMethod.POST,
      byggKomplettVedtakRequest(),
      NyttVedtakResponse::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull() }
    )
  }

  private fun fullUrlForNyttVedtak(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + VedtakController.VEDTAK_NY).toUriString()
  }

  private fun fullUrlForNyttKomplettVedtak(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + VedtakController.VEDTAK_NY_KOMPLETT).toUriString()
  }

  private fun fullUrlForSokVedtak(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + VedtakController.VEDTAK_SOK).toUriString()
  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port$contextPath"
  }

  private fun byggRequest(): HttpEntity<NyttVedtakRequest> {
    return initHttpEntity(NyttVedtakRequest(saksbehandlerId = "TEST", enhetId = "1111"))
  }

  private fun byggKomplettVedtakRequest(): HttpEntity<NyttKomplettVedtakRequest> {
    return initHttpEntity(TestUtil.byggKomplettVedtakRequest())
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}
