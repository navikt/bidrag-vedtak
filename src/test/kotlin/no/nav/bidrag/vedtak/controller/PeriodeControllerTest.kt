package no.nav.bidrag.vedtak.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.BidragVedtakLocal.Companion.TEST_PROFILE
import no.nav.bidrag.vedtak.api.AllePerioderForStonadsendringResponse
import no.nav.bidrag.vedtak.api.NyPeriodeRequest
import no.nav.bidrag.vedtak.dto.PeriodeDto
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import no.nav.bidrag.vedtak.dto.VedtakDto
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
import java.math.BigDecimal
import java.time.LocalDate

@DisplayName("PeriodeControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = WebEnvironment.RANDOM_PORT)
class PeriodeControllerTest {

  @Autowired
  private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

  @Autowired
  private lateinit var vedtakRepository: VedtakRepository

  @Autowired
  private lateinit var stonadsendringRepository: StonadsendringRepository

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @LocalServerPort
  private val port = 0

  @Value("\${server.servlet.context-path}")
  private val contextPath: String? = null

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal mappe til context path med random port`() {
    assertThat(makeFullContextPath()).isEqualTo("http://localhost:$port/bidrag-vedtak")
  }


  @Test
  fun `skal opprette ny periode`() {

    // Oppretter ny forekomst av vedtak
    val nyttVedtakOpprettet = persistenceService.opprettNyttVedtak(VedtakDto(opprettetAv = "TEST", enhetsnummer = "1111"))

    // Oppretter ny forekomst av stonadsendring
    val nyStonadsendringOpprettet = persistenceService.opprettNyStonadsendring(StonadsendringDto(
      stonadType = "BIDRAG", vedtakId = nyttVedtakOpprettet.vedtakId, behandlingId = "1111", skyldnerId = "1111",
      kravhaverId = "1111", mottakerId = "1111", opprettetAv = "TEST")
    )

/*    // Oppretter ny forekomst av periode
    val nyPeriodeOpprettet = persistenceService.opprettNyPeriode(
      PeriodeDto(
        periodeFom = LocalDate.now(),
        periodeTom = LocalDate.now(),
        stonadsendringId = nyStonadsendringOpprettet.stonadsendringId,
        belop = BigDecimal.valueOf(17.01),
        valutakode = "NOK",
        resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER",
        opprettetAv = "TEST"
      )
    )*/

    // Oppretter ny forekomst av periode
    val response = securedTestRestTemplate.exchange(
      fullUrlForNyPeriode(),
      HttpMethod.POST,
      byggRequest(nyStonadsendringOpprettet.stonadsendringId),
      PeriodeDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull() },
      Executable { assertThat(response?.body?.stonadsendringId).isEqualTo(nyStonadsendringOpprettet.stonadsendringId) },
      Executable { assertThat(response?.body?.belop).isEqualTo(BigDecimal.valueOf(17.01)) },
      Executable { assertThat(response?.body?.resultatkode).isEqualTo("RESULTATKODE_TEST") }

    )

    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()

  }

  @Test
  fun `skal finne data for en periode`(){
    // Oppretter ny forekomst av vedtak
    val nyttVedtakOpprettet = persistenceService.opprettNyttVedtak(VedtakDto(opprettetAv = "TEST", enhetsnummer = "1111"))

    // Oppretter ny forekomst av stonadsendring
    val nyStonadsendringOpprettet = persistenceService.opprettNyStonadsendring(StonadsendringDto(
      stonadType = "BIDRAG", vedtakId = nyttVedtakOpprettet.vedtakId, behandlingId = "1111", skyldnerId = "1111",
      kravhaverId = "1111", mottakerId = "1111", opprettetAv = "TEST"
    )
    )

    // Oppretter ny forekomst av periode
    val nyPeriodeOpprettet = persistenceService.opprettNyPeriode(
      PeriodeDto(
        periodeFom = LocalDate.now(),
        periodeTom = LocalDate.now(),
        stonadsendringId = nyStonadsendringOpprettet.stonadsendringId,
        belop = BigDecimal.valueOf(17.01),
        valutakode = "NOK",
        resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER",
        opprettetAv = "TEST"
      )
    )

    // Henter forekomst
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokPeriode()}/${nyPeriodeOpprettet.periodeId}",
      HttpMethod.GET,
      null,
      PeriodeDto::class.java)


    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.periodeId).isEqualTo(nyPeriodeOpprettet.periodeId) },
      Executable { assertThat(response?.body?.stonadsendringId).isEqualTo(nyPeriodeOpprettet.stonadsendringId) },
      Executable { assertThat(response?.body?.belop).isEqualTo(nyPeriodeOpprettet.belop) },
      Executable { assertThat(response?.body?.stonadsendringId).isEqualTo(nyPeriodeOpprettet.stonadsendringId) },
      Executable { assertThat(response?.body?.opprettetAv).isEqualTo(nyPeriodeOpprettet.opprettetAv) }
    )

    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()

  }

  @Test
  fun `skal finne alle perioder for stonadsendring`(){
    // Oppretter ny forekomst av vedtak
    val nyttVedtakOpprettet = persistenceService.opprettNyttVedtak(VedtakDto(opprettetAv = "TEST", enhetsnummer = "1111"))

    // Oppretter ny forekomst av stonadsendring
    val nyStonadsendringOpprettet = persistenceService.opprettNyStonadsendring(StonadsendringDto(
      stonadType = "BIDRAG", vedtakId = nyttVedtakOpprettet.vedtakId, behandlingId = "1111", skyldnerId = "1111",
      kravhaverId = "1111", mottakerId = "1111", opprettetAv = "TEST"
    )
    )

    // Oppretter nye forekomster av periode
    val nyPeriodeOpprettet1 = persistenceService.opprettNyPeriode(
      PeriodeDto(
        periodeFom = LocalDate.now(),
        periodeTom = LocalDate.now(),
        stonadsendringId = nyStonadsendringOpprettet.stonadsendringId,
        belop = BigDecimal.valueOf(17.01),
        valutakode = "NOK",
        resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER",
        opprettetAv = "TEST"
      )
    )

    val nyPeriodeOpprettet2 = persistenceService.opprettNyPeriode(
      PeriodeDto(
        periodeFom = LocalDate.now(),
        periodeTom = LocalDate.now(),
        stonadsendringId = nyStonadsendringOpprettet.stonadsendringId,
        belop = BigDecimal.valueOf(2000.02),
        valutakode = "NOK",
        resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER",
        opprettetAv = "TEST"
      )
    )

    // Henter forekomst
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokPerioderForStonadsendring()}/${nyStonadsendringOpprettet.stonadsendringId}",
      HttpMethod.GET,
      null,
      AllePerioderForStonadsendringResponse::class.java)


    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.allePerioderForStonadsendring!![0].periodeId).isEqualTo(nyPeriodeOpprettet1.periodeId) },
      Executable { assertThat(response?.body?.allePerioderForStonadsendring!![0].stonadsendringId).isEqualTo(nyPeriodeOpprettet1.stonadsendringId) },
      Executable { assertThat(response?.body?.allePerioderForStonadsendring!![1].belop).isEqualTo(nyPeriodeOpprettet2.belop) },
      Executable { assertThat(response?.body?.allePerioderForStonadsendring!![1].stonadsendringId).isEqualTo(nyPeriodeOpprettet2.stonadsendringId) },
      Executable { assertThat(response?.body?.allePerioderForStonadsendring!![0].opprettetAv).isEqualTo(nyPeriodeOpprettet1.opprettetAv) }
    )

    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()

  }



  private fun fullUrlForNyPeriode(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + PeriodeController.PERIODE_NY).toUriString()
  }

  private fun fullUrlForSokPeriode(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + PeriodeController.PERIODE_SOK).toUriString()
  }

  private fun fullUrlForSokPerioderForStonadsendring(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + PeriodeController.PERIODE_SOK_STONADSENDRING).toUriString()
  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port$contextPath"
  }

  private fun byggRequest(stonadsendringId: Int): HttpEntity<NyPeriodeRequest> {
    return initHttpEntity(NyPeriodeRequest(
      LocalDate.now(), LocalDate.now(), stonadsendringId, BigDecimal.valueOf(17.01),"NOK", "RESULTATKODE_TEST",
    "TEST"))
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}
