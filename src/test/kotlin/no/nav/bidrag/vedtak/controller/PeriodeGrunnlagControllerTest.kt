package no.nav.bidrag.vedtak.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.BidragVedtakLocal.Companion.TEST_PROFILE
import no.nav.bidrag.vedtak.api.periodegrunnlag.OpprettPeriodeGrunnlagRequest
import no.nav.bidrag.vedtak.dto.GrunnlagDto
import no.nav.bidrag.vedtak.dto.PeriodeDto
import no.nav.bidrag.vedtak.dto.PeriodeGrunnlagDto
import no.nav.bidrag.vedtak.dto.StonadsendringDto
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
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder
import java.math.BigDecimal
import java.time.LocalDate

@DisplayName("PeriodeGrunnlagControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = WebEnvironment.RANDOM_PORT)
class PeriodeGrunnlagControllerTest {

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

  private val periodeGrunnlagDtoListe = object : ParameterizedTypeReference<List<PeriodeGrunnlagDto>>() {}

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    periodeGrunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
    grunnlagRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }


  @Test
  fun `skal opprette nytt periodegrunnlag`() {

    // Oppretter ny forekomst av vedtak
    val nyttVedtakOpprettet = persistenceService.opprettVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter ny forekomst av stonadsendring
    val nyStonadsendringOpprettet = persistenceService.opprettStonadsendring(StonadsendringDto(
      stonadType = "BIDRAG",
      vedtakId = nyttVedtakOpprettet.vedtakId,
      behandlingId = "1111",
      skyldnerId = "1111",
      kravhaverId = "1111",
      mottakerId = "1111")
    )

    val nyttGrunnlagOpprettet = persistenceService.opprettGrunnlag(
      GrunnlagDto(
        grunnlagReferanse = "",
        vedtakId = nyttVedtakOpprettet.vedtakId,
        grunnlagType = "Beregnet Inntekt",
        grunnlagInnhold = "100")
    )

    // Oppretter ny forekomst av periode
    val nyPeriodeOpprettet = persistenceService.opprettPeriode(
      PeriodeDto(
        periodeFomDato = LocalDate.now(),
        periodeTilDato = LocalDate.now(),
        stonadsendringId = nyStonadsendringOpprettet.stonadsendringId,
        belop = BigDecimal.valueOf(17.01),
        valutakode = "NOK",
        resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER")
    )

    // Oppretter ny forekomst av periodeGrunnlag
    val response = securedTestRestTemplate.exchange(
      fullUrlForNyttPeriodeGrunnlag(),
      HttpMethod.POST,
      byggRequest(nyPeriodeOpprettet.periodeId, nyttGrunnlagOpprettet.grunnlagId),
      PeriodeGrunnlagDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.periodeId).isEqualTo(nyPeriodeOpprettet.periodeId) },
      Executable { assertThat(response?.body?.grunnlagId).isEqualTo(nyttGrunnlagOpprettet.grunnlagId) },

      )
    periodeGrunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
    grunnlagRepository.deleteAll()
  }

  @Test
  fun `skal hente alle grunnlag for en periode`() {
    // Oppretter ny forekomst av vedtak
    val nyttVedtakOpprettet = persistenceService.opprettVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter nye forekomster av stønadsendring
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

    // Oppretter ny forekomst av periode
    val nyPeriodeOpprettet = persistenceService.opprettPeriode(
      PeriodeDto(
        periodeFomDato = LocalDate.now(),
        periodeTilDato = LocalDate.now(),
        stonadsendringId = nyStonadsendringOpprettet.stonadsendringId,
        belop = BigDecimal.valueOf(17.01),
        valutakode = "NOK",
        resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER")
    )

    val nyttGrunnlagOpprettet1 = persistenceService.opprettGrunnlag(
      GrunnlagDto(
        grunnlagReferanse = "",
        vedtakId = nyttVedtakOpprettet.vedtakId,
        grunnlagType = "Beregnet Inntekt",
        grunnlagInnhold = "100")
    )
    val nyttGrunnlagOpprettet2 = persistenceService.opprettGrunnlag(
      GrunnlagDto(
        grunnlagReferanse = "",
        vedtakId = nyttVedtakOpprettet.vedtakId,
        grunnlagType = "Beregnet Skatt",
        grunnlagInnhold = "10")
    )

    // Oppretter ny forekomst av periodeGrunnlag
    val nyttPeriodeGrunnlagOpprettet1 = persistenceService.opprettPeriodeGrunnlag(
      PeriodeGrunnlagDto(
        periodeId = nyPeriodeOpprettet.periodeId,
        grunnlagId = nyttGrunnlagOpprettet1.grunnlagId,
        grunnlagValgt = true)
    )
    // Oppretter ny forekomst av periodeGrunnlag
    val nyttPeriodeGrunnlagOpprettet2 = persistenceService.opprettPeriodeGrunnlag(
      PeriodeGrunnlagDto(
        periodeId = nyPeriodeOpprettet.periodeId,
        grunnlagId = nyttGrunnlagOpprettet2.grunnlagId,
        grunnlagValgt = false)
    )

    // Henter forekomster
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokAllePeriodeGrunnlagForPeriode()}/${nyPeriodeOpprettet.periodeId}",
      HttpMethod.GET,
      null,
      periodeGrunnlagDtoListe
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response.body).isNotNull },
      Executable { assertThat(response.body?.size).isEqualTo(2) },
      Executable { assertThat(response.body?.get(0)?.periodeId).isEqualTo(nyttPeriodeGrunnlagOpprettet1.periodeId) },
      Executable { assertThat(response.body?.get(1)?.periodeId).isEqualTo(nyttPeriodeGrunnlagOpprettet2.periodeId) },
      Executable { assertThat(response.body?.get(0)?.grunnlagId).isEqualTo(nyttPeriodeGrunnlagOpprettet1.grunnlagId) },
      Executable { assertThat(response.body?.get(1)?.grunnlagId).isEqualTo(nyttPeriodeGrunnlagOpprettet2.grunnlagId) },
      Executable { assertThat(response.body?.get(0)?.grunnlagValgt).isEqualTo(nyttPeriodeGrunnlagOpprettet1.grunnlagValgt) },
      Executable { assertThat(response.body?.get(1)?.grunnlagValgt).isEqualTo(nyttPeriodeGrunnlagOpprettet2.grunnlagValgt) },

      )
    periodeGrunnlagRepository.deleteAll()
    grunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
  }

  private fun fullUrlForNyttPeriodeGrunnlag(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + PeriodeGrunnlagController.OPPRETT_PERIODEGRUNNLAG).toUriString()
  }

  private fun fullUrlForSokAllePeriodeGrunnlagForPeriode(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + PeriodeGrunnlagController.HENT_PERIODEGRUNNLAG_FOR_PERIODE).toUriString()
  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port$contextPath"
  }

  private fun byggRequest(periodeId: Int, grunnlagId: Int): HttpEntity<OpprettPeriodeGrunnlagRequest> {
    return initHttpEntity(OpprettPeriodeGrunnlagRequest(
      periodeId,
      grunnlagId,
      true)
    )
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}