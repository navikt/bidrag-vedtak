package no.nav.bidrag.vedtak.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.BidragVedtakLocal.Companion.TEST_PROFILE
import no.nav.bidrag.vedtak.api.engangsbelop.OpprettKomplettEngangsbelopRequest
import no.nav.bidrag.vedtak.dto.EngangsbelopDto
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.persistence.repository.BehandlingsreferanseRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbelopGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbelopRepository
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

@DisplayName("EngangsbelopControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = WebEnvironment.RANDOM_PORT)
class EngangsbelopControllerTest {

  @Autowired
  private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

  @Autowired
  private lateinit var behandlingsreferanseRepository: BehandlingsreferanseRepository

  @Autowired
  private lateinit var stonadsendringRepository: StonadsendringRepository

  @Autowired
  private lateinit var engangsbelopRepository: EngangsbelopRepository

  @Autowired
  private lateinit var engangsbelopGrunnlagRepository: EngangsbelopGrunnlagRepository

  @Autowired
  private lateinit var vedtakRepository: VedtakRepository

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var grunnlagRepository: GrunnlagRepository

  @Autowired
  private lateinit var periodeGrunnlagRepository: PeriodeGrunnlagRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @LocalServerPort
  private val port = 0

  @Value("\${server.servlet.context-path}")
  private val contextPath: String? = null

  private val engangsbelopDtoListe = object : ParameterizedTypeReference<List<EngangsbelopDto>>() {}

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    behandlingsreferanseRepository.deleteAll()
    engangsbelopGrunnlagRepository.deleteAll()
    engangsbelopRepository.deleteAll()
    periodeGrunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
    grunnlagRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal mappe til context path med random port`() {
    assertThat(makeFullContextPath()).isEqualTo("http://localhost:$port/bidrag-vedtak")
  }

  @Test
  fun `skal opprette nytt engangsbelop`() {
    // Oppretter ny forekomst av vedtak
    val nyttVedtakOpprettet = persistenceService.opprettVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter ny forekomst av engangsbelop
    val response = securedTestRestTemplate.exchange(
      fullUrlForNyttEngangsbelop(),
      HttpMethod.POST,
      byggRequest(nyttVedtakOpprettet.vedtakId),
      EngangsbelopDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull() },
      Executable { assertThat(response?.body?.vedtakId).isEqualTo(nyttVedtakOpprettet.vedtakId) },
      Executable { assertThat(response?.body?.lopenr).isEqualTo(1) },
      Executable { assertThat(response?.body?.belop).isEqualTo(BigDecimal.valueOf(17.0)) }
    )
/*    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()*/
  }

  @Test
  fun `skal hente data for et engangsbelop`() {
    // Oppretter ny forekomst av vedtak
    val nyttVedtakOpprettet = persistenceService.opprettVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter ny forekomst av engangsbelop
    val nyttEngangsbelopOpprettet = persistenceService.opprettEngangsbelop(
      EngangsbelopDto(
        vedtakId = nyttVedtakOpprettet.vedtakId,
        lopenr = 1,
        endrerEngangsbelopId = null,
        type = "SAERBIDRAG",
        skyldnerId = "1111",
        kravhaverId = "1111",
        mottakerId = "1111",
        belop = BigDecimal.valueOf(17.00),
        valutakode = "NOK",
        resultatkode = "Alles gut"
      )
    )

    // Henter forekomst
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokEngangsbelop()}/${nyttEngangsbelopOpprettet.engangsbelopId}",
      HttpMethod.GET,
      null,
      EngangsbelopDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.engangsbelopId).isEqualTo(nyttEngangsbelopOpprettet.engangsbelopId) },
      Executable { assertThat(response?.body?.vedtakId).isEqualTo(nyttEngangsbelopOpprettet.vedtakId) },
      Executable { assertThat(response?.body?.lopenr).isEqualTo(nyttEngangsbelopOpprettet.lopenr) },
      Executable { assertThat(response?.body?.belop).isEqualByComparingTo(nyttEngangsbelopOpprettet.belop) }
    )
/*    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()*/
  }

  @Test
  fun `skal hente alle engangsbelop for et vedtak`() {
    // Oppretter ny forekomst av vedtak
    val nyttVedtakOpprettet1 = persistenceService.opprettVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))
    val nyttVedtakOpprettet2 = persistenceService.opprettVedtak(VedtakDto(17, saksbehandlerId = "TEST", enhetId = "9999"))

    // Oppretter nye forekomster av engangsbelop
    val nyttEngangsbelopOpprettet1 = persistenceService.opprettEngangsbelop(
      EngangsbelopDto(
        vedtakId = nyttVedtakOpprettet1.vedtakId,
        lopenr = 1,
        endrerEngangsbelopId = null,
        type = "SAERBIDRAG",
        skyldnerId = "1111",
        kravhaverId = "1111",
        mottakerId = "1111",
        belop = BigDecimal.valueOf(17.0),
        valutakode = "NOK",
        resultatkode = "SAERBIDRAG_BEREGNET"
      )
    )

    val nyttEngangsbelopOpprettet2 = persistenceService.opprettEngangsbelop(
      EngangsbelopDto(
        vedtakId = nyttVedtakOpprettet1.vedtakId,
        lopenr = nyttEngangsbelopOpprettet1.lopenr + 1,
        endrerEngangsbelopId = nyttEngangsbelopOpprettet1.engangsbelopId,
        type = "SAERBIDRAG",
        skyldnerId = "1111",
        kravhaverId = "1111",
        mottakerId = "1111",
        belop = BigDecimal.valueOf(666.0),
        valutakode = "NOK",
        resultatkode = "SAERBIDRAG_BELOP_ENDRET_ETTER_KLAGE"
      )
    )

    // Engangsbelop som ikke skal legges med i resultatet
    persistenceService.opprettEngangsbelop(
      EngangsbelopDto(
        vedtakId = nyttVedtakOpprettet2.vedtakId,
        lopenr = nyttEngangsbelopOpprettet2.lopenr + 1,
        endrerEngangsbelopId = nyttEngangsbelopOpprettet2.engangsbelopId,
        type = "SAERBIDRAG",
        skyldnerId = "1111",
        kravhaverId = "1111",
        mottakerId = "1111",
        belop = BigDecimal.valueOf(999.0),
        valutakode = "NOK",
        resultatkode = "SAERBIDRAG_BEREGNET"
      )
    )

    // Henter forekomster
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokEngangsbelopForVedtak()}/${nyttVedtakOpprettet1.vedtakId}",
      HttpMethod.GET,
      null,
      engangsbelopDtoListe
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response.body).isNotNull },
      Executable { assertThat(response.body?.size).isEqualTo(2) },
      Executable { assertThat(response.body?.get(0)?.engangsbelopId).isEqualTo(nyttEngangsbelopOpprettet1.engangsbelopId) },
      Executable { assertThat(response.body?.get(0)?.vedtakId).isEqualTo(nyttEngangsbelopOpprettet1.vedtakId) },
      Executable { assertThat(response.body?.get(0)?.lopenr).isEqualTo(nyttEngangsbelopOpprettet1.lopenr) },
      Executable { assertThat(response.body?.get(0)?.belop).isEqualByComparingTo(nyttEngangsbelopOpprettet1.belop) },
      Executable { assertThat(response.body?.get(0)?.resultatkode).isEqualTo(nyttEngangsbelopOpprettet1.resultatkode) },
      Executable { assertThat(response.body?.get(1)?.engangsbelopId).isEqualTo(nyttEngangsbelopOpprettet2.engangsbelopId) },
      Executable { assertThat(response.body?.get(1)?.vedtakId).isEqualTo(nyttEngangsbelopOpprettet2.vedtakId) },
      Executable { assertThat(response.body?.get(1)?.lopenr).isEqualTo(nyttEngangsbelopOpprettet2.lopenr) },
      Executable { assertThat(response.body?.get(1)?.belop).isEqualByComparingTo(nyttEngangsbelopOpprettet2.belop) },
      Executable { assertThat(response.body?.get(1)?.resultatkode).isEqualTo(nyttEngangsbelopOpprettet2.resultatkode) }
    )
/*    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()*/
  }

  private fun fullUrlForNyttEngangsbelop(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + EngangsbelopController.OPPRETT_ENGANGSBELOP).toUriString()
  }

  private fun fullUrlForSokEngangsbelop(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + EngangsbelopController.HENT_ENGANGSBELOP).toUriString()
  }

  private fun fullUrlForSokEngangsbelopForVedtak(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + EngangsbelopController.HENT_ENGANGSBELOP_FOR_VEDTAK).toUriString()
  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port$contextPath"
  }

  private fun byggRequest(vedtakId: Int): HttpEntity<OpprettKomplettEngangsbelopRequest> {
    return initHttpEntity(OpprettKomplettEngangsbelopRequest(
      vedtakId,
      1,
      null,
      "SAERBIDRAG",
      "1111",
      "1111",
      "1111",
      BigDecimal.valueOf(17.0),
      "NOK",
      "Alles gut"
    ))
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}