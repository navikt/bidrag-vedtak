package no.nav.bidrag.vedtak.controller

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.BidragVedtakLocal.Companion.TEST_PROFILE
import no.nav.bidrag.vedtak.api.engangsbelopgrunnlag.OpprettEngangsbelopGrunnlagRequest
import no.nav.bidrag.vedtak.api.periodegrunnlag.OpprettPeriodeGrunnlagRequest
import no.nav.bidrag.vedtak.dto.EngangsbelopDto
import no.nav.bidrag.vedtak.dto.EngangsbelopGrunnlagDto
import no.nav.bidrag.vedtak.dto.GrunnlagDto
import no.nav.bidrag.vedtak.dto.PeriodeDto
import no.nav.bidrag.vedtak.dto.PeriodeGrunnlagDto
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.persistence.repository.EngangsbelopGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbelopRepository
import no.nav.bidrag.vedtak.persistence.repository.GrunnlagRepository
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

@DisplayName("EngangsbelopGrunnlagControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = WebEnvironment.RANDOM_PORT)
class EngangsbelopGrunnlagControllerTest {

  @Autowired
  private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

  @Autowired
  private lateinit var engangsbelopGrunnlagRepository: EngangsbelopGrunnlagRepository

  @Autowired
  private lateinit var engangsbelopRepository: EngangsbelopRepository

  @Autowired
  private lateinit var grunnlagRepository: GrunnlagRepository

  @Autowired
  private lateinit var vedtakRepository: VedtakRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @LocalServerPort
  private val port = 0

  @Value("\${server.servlet.context-path}")
  private val contextPath: String? = null

  private val engangsbelopGrunnlagDtoListe = object : ParameterizedTypeReference<List<EngangsbelopGrunnlagDto>>() {}

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    engangsbelopGrunnlagRepository.deleteAll()
    engangsbelopRepository.deleteAll()
    grunnlagRepository.deleteAll()
    vedtakRepository.deleteAll()
  }


  @Test
  fun `skal opprette nytt engangsbelopgrunnlag`() {

    // Oppretter ny forekomst av vedtak
    val nyttVedtakOpprettet = persistenceService.opprettVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter ny forekomst av engangsbelop
    val nyttEngangsbelopOpprettet = persistenceService.opprettEngangsbelop(EngangsbelopDto(
      vedtakId = nyttVedtakOpprettet.vedtakId,
      lopenr = 1,
      endrerEngangsbelopId = null,
      type = "SAERBIDRAG",
      skyldnerId = "1111",
      kravhaverId = "1111",
      mottakerId = "1111",
      belop = BigDecimal.valueOf(17.0),
      valutakode = "NOK",
      resultatkode = "SAERBIDRAG_BEREGNET")
    )

    val nyttGrunnlagOpprettet = persistenceService.opprettGrunnlag(
      GrunnlagDto(
        grunnlagReferanse = "",
        vedtakId = nyttVedtakOpprettet.vedtakId,
        grunnlagType = "Beregnet Inntekt",
        grunnlagInnhold = "100")
    )

    // Oppretter ny forekomst av periodeGrunnlag
    val response = securedTestRestTemplate.exchange(
      fullUrlForNyttEngangsbelopGrunnlag(),
      HttpMethod.POST,
      byggRequest(nyttEngangsbelopOpprettet.engangsbelopId, nyttGrunnlagOpprettet.grunnlagId),
      EngangsbelopGrunnlagDto::class.java
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response?.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response?.body).isNotNull },
      Executable { assertThat(response?.body?.engangsbelopId).isEqualTo(nyttEngangsbelopOpprettet.engangsbelopId) },
      Executable { assertThat(response?.body?.grunnlagId).isEqualTo(nyttGrunnlagOpprettet.grunnlagId) },

      )
    engangsbelopGrunnlagRepository.deleteAll()
    engangsbelopRepository.deleteAll()
    grunnlagRepository.deleteAll()
  }

  @Test
  fun `skal hente alle grunnlag for et engangsbelop`() {
    // Oppretter ny forekomst av vedtak
    val nyttVedtakOpprettet = persistenceService.opprettVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter nye forekomster av stønadsendring
    val nyttEngangsbelopOpprettet = persistenceService.opprettEngangsbelop(
      EngangsbelopDto(
        vedtakId = nyttVedtakOpprettet.vedtakId,
        lopenr = 1,
        endrerEngangsbelopId = null,
        type = "SAERBIDRAG",
        skyldnerId = "1111",
        kravhaverId = "1111",
        mottakerId = "1111",
        belop = BigDecimal.valueOf(17.0),
        valutakode = "NOK",
        resultatkode = "SAERBIDRAG_BEREGNET")
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

    // Oppretter ny forekomst av EngangsbelopGrunnlag
    val nyttEngangsbelopGrunnlagOpprettet1 = persistenceService.opprettEngangsbelopGrunnlag(
      EngangsbelopGrunnlagDto(
        engangsbelopId = nyttEngangsbelopOpprettet.engangsbelopId,
        grunnlagId = nyttGrunnlagOpprettet1.grunnlagId
      )
    )
    // Oppretter ny forekomst av EngangsbelopGrunnlag
    val nyttEngangsbelopGrunnlagOpprettet2 = persistenceService.opprettEngangsbelopGrunnlag(
      EngangsbelopGrunnlagDto(
        engangsbelopId = nyttEngangsbelopOpprettet.engangsbelopId,
        grunnlagId = nyttGrunnlagOpprettet2.grunnlagId
      )
    )

    // Henter forekomster
    val response = securedTestRestTemplate.exchange(
      "${fullUrlForSokAlleGrunnlagForEngangsbelop()}/${nyttEngangsbelopOpprettet.engangsbelopId}",
      HttpMethod.GET,
      null,
      engangsbelopGrunnlagDtoListe
    )

    assertAll(
      Executable { assertThat(response).isNotNull() },
      Executable { assertThat(response.statusCode).isEqualTo(HttpStatus.OK) },
      Executable { assertThat(response.body).isNotNull },
      Executable { assertThat(response.body?.size).isEqualTo(2) },
      Executable { assertThat(response.body?.get(0)?.engangsbelopId).isEqualTo(nyttEngangsbelopGrunnlagOpprettet1.engangsbelopId) },
      Executable { assertThat(response.body?.get(1)?.engangsbelopId).isEqualTo(nyttEngangsbelopGrunnlagOpprettet2.engangsbelopId) },
      Executable { assertThat(response.body?.get(0)?.grunnlagId).isEqualTo(nyttEngangsbelopGrunnlagOpprettet1.grunnlagId) },
      Executable { assertThat(response.body?.get(1)?.grunnlagId).isEqualTo(nyttEngangsbelopGrunnlagOpprettet2.grunnlagId) }
      )
    engangsbelopGrunnlagRepository.deleteAll()
    engangsbelopRepository.deleteAll()
    grunnlagRepository.deleteAll()
  }

  private fun fullUrlForNyttEngangsbelopGrunnlag(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + EngangsbelopGrunnlagController.OPPRETT_ENGANGSBELOPGRUNNLAG).toUriString()
  }

  private fun fullUrlForSokAlleGrunnlagForEngangsbelop(): String {
    return UriComponentsBuilder.fromHttpUrl(makeFullContextPath() + EngangsbelopGrunnlagController.HENT_GRUNNLAG_FOR_ENGANGSBELOP).toUriString()
  }

  private fun makeFullContextPath(): String {
    return "http://localhost:$port$contextPath"
  }

  private fun byggRequest(engangsbelopId: Int, grunnlagId: Int): HttpEntity<OpprettEngangsbelopGrunnlagRequest> {
    return initHttpEntity(OpprettEngangsbelopGrunnlagRequest(
      engangsbelopId,
      grunnlagId
    )
    )
  }

  private fun <T> initHttpEntity(body: T): HttpEntity<T> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_JSON
    return HttpEntity(body, httpHeaders)
  }
}