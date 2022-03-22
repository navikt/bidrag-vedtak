package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.BidragVedtakTest
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtakRequest
import no.nav.bidrag.vedtak.persistence.repository.BehandlingsreferanseRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbelopGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbelopRepository
import no.nav.bidrag.vedtak.persistence.repository.GrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeRepository
import no.nav.bidrag.vedtak.persistence.repository.StonadsendringRepository
import no.nav.bidrag.vedtak.persistence.repository.VedtakRepository
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles

@DisplayName("VedtakServiceTest")
@ActiveProfiles(BidragVedtakTest.TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakTest::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
@AutoConfigureWireMock(port = 0)
class VedtakServiceTest {

  @Autowired
  private lateinit var vedtakService: VedtakService

  @Autowired
  private lateinit var behandlingsreferanseRepository: BehandlingsreferanseRepository

  @Autowired
  private lateinit var engangsbelopGrunnlagRepository: EngangsbelopGrunnlagRepository

  @Autowired
  private lateinit var periodeGrunnlagRepository: PeriodeGrunnlagRepository

  @Autowired
  private lateinit var grunnlagRepository: GrunnlagRepository

  @Autowired
  private lateinit var engangsbelopRepository: EngangsbelopRepository

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var stonadsendringRepository: StonadsendringRepository

  @Autowired
  private lateinit var vedtakRepository: VedtakRepository


  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    behandlingsreferanseRepository.deleteAll()
    engangsbelopGrunnlagRepository.deleteAll()
    periodeGrunnlagRepository.deleteAll()
    engangsbelopRepository.deleteAll()
    grunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal opprette og hente vedtak`() {
    // Oppretter nytt vedtak
    val nyttVedtakRequest = byggVedtakRequest()
    val nyttVedtakOpprettet = vedtakService.opprettVedtak(nyttVedtakRequest)

    assertAll(
      Executable { assertThat(nyttVedtakOpprettet).isNotNull() }
    )

    // Henter vedtak
    val vedtakFunnet = vedtakService.hentVedtak(nyttVedtakOpprettet)

    assertAll(
      Executable { assertThat(vedtakFunnet).isNotNull() },

      // Vedtak
      Executable { assertThat(vedtakFunnet.vedtakId).isNotNull() },
      Executable { assertThat(vedtakFunnet.vedtakType).isEqualTo(nyttVedtakRequest.vedtakType) },
      Executable { assertThat(vedtakFunnet.opprettetTimestamp).isNotNull() },
      Executable { assertThat(vedtakFunnet.opprettetAv).isEqualTo(nyttVedtakRequest.opprettetAv) },
      Executable { assertThat(vedtakFunnet.vedtakDato).isEqualTo(nyttVedtakRequest.vedtakDato) },
      Executable { assertThat(vedtakFunnet.enhetId).isEqualTo(nyttVedtakRequest.enhetId) },
      Executable { assertThat(vedtakFunnet.grunnlagListe.size).isEqualTo(4) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe.size).isEqualTo(2) },
      Executable { assertThat(vedtakFunnet.behandlingsreferanseListe.size).isEqualTo(2) },

      // Grunnlag
      Executable { assertThat(vedtakFunnet.grunnlagListe[0].referanse).isEqualTo(nyttVedtakRequest.grunnlagListe[0].referanse) },
      Executable { assertThat(vedtakFunnet.grunnlagListe[0].grunnlagType).isEqualTo(nyttVedtakRequest.grunnlagListe[0].grunnlagType) },
      Executable { assertThat(vedtakFunnet.grunnlagListe[0].grunnlagInnhold).isEqualTo(nyttVedtakRequest.grunnlagListe[0].grunnlagInnhold.toString()) },

      Executable { assertThat(vedtakFunnet.grunnlagListe[1].referanse).isEqualTo(nyttVedtakRequest.grunnlagListe[1].referanse) },
      Executable { assertThat(vedtakFunnet.grunnlagListe[1].grunnlagType).isEqualTo(nyttVedtakRequest.grunnlagListe[1].grunnlagType) },
      Executable { assertThat(vedtakFunnet.grunnlagListe[1].grunnlagInnhold).isEqualTo(nyttVedtakRequest.grunnlagListe[1].grunnlagInnhold.toString()) },

      Executable { assertThat(vedtakFunnet.grunnlagListe[2].referanse).isEqualTo(nyttVedtakRequest.grunnlagListe[2].referanse) },
      Executable { assertThat(vedtakFunnet.grunnlagListe[2].grunnlagType).isEqualTo(nyttVedtakRequest.grunnlagListe[2].grunnlagType) },
      Executable { assertThat(vedtakFunnet.grunnlagListe[2].grunnlagInnhold).isEqualTo(nyttVedtakRequest.grunnlagListe[2].grunnlagInnhold.toString()) },

      Executable { assertThat(vedtakFunnet.grunnlagListe[3].referanse).isEqualTo(nyttVedtakRequest.grunnlagListe[3].referanse) },
      Executable { assertThat(vedtakFunnet.grunnlagListe[3].grunnlagType).isEqualTo(nyttVedtakRequest.grunnlagListe[3].grunnlagType) },
      Executable { assertThat(vedtakFunnet.grunnlagListe[3].grunnlagInnhold).isEqualTo(nyttVedtakRequest.grunnlagListe[3].grunnlagInnhold.toString()) },

      // Stonadsendring
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].stonadType).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].stonadType) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].sakId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].sakId) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].behandlingId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].behandlingId) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].skyldnerId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].skyldnerId) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].kravhaverId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].kravhaverId) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].mottakerId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].mottakerId) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe.size).isEqualTo(2) },

      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].stonadType).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].stonadType) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].sakId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].sakId) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].behandlingId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].behandlingId) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].skyldnerId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].skyldnerId) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].kravhaverId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].kravhaverId) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].mottakerId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].mottakerId) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe.size).isEqualTo(2) },

      // Periode
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].periodeFomDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].periodeFomDato) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].periodeTilDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].periodeTilDato) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].belop.toInt()).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].belop.toInt()) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].valutakode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].valutakode) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].resultatkode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].resultatkode) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(3) },

      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].periodeFomDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].periodeFomDato) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].periodeTilDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].periodeTilDato) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].belop.toInt()).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].belop.toInt()) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].valutakode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].valutakode) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].resultatkode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].resultatkode) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(4) },

      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].periodeFomDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].periodeFomDato) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].periodeTilDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].periodeTilDato) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].belop.toInt()).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].belop.toInt()) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].valutakode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].valutakode) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].resultatkode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].resultatkode) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(2) },

      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].periodeFomDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].periodeFomDato) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].periodeTilDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].periodeTilDato) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].belop.toInt()).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].belop.toInt()) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].valutakode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].valutakode) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].resultatkode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].resultatkode) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(2) },

      // GrunnlagReferanse
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0].referanse).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[0].referanse) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[1].referanse).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[1].referanse) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[2].referanse).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[2].referanse) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[0].referanse).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[0].referanse) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[1].referanse).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[1].referanse) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[2].referanse).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[2].referanse) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[3].referanse).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[3].referanse) },

      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe[0].referanse).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].grunnlagReferanseListe[0].referanse) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe[1].referanse).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].grunnlagReferanseListe[1].referanse) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe[0].referanse).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].grunnlagReferanseListe[0].referanse) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe[1].referanse).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].grunnlagReferanseListe[1].referanse) },

      // Engangsbeløp
      Executable { assertThat(vedtakFunnet.engangsbelopListe.size).isEqualTo(2) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].endrerEngangsbelopId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].endrerEngangsbelopId) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].type).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].type) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].skyldnerId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].skyldnerId) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].kravhaverId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].kravhaverId) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].mottakerId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].mottakerId) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].belop.toInt()).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].belop.toInt()) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].valutakode).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].valutakode) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].resultatkode).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].resultatkode) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].grunnlagReferanseListe.size).isEqualTo(3) },

      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].endrerEngangsbelopId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].endrerEngangsbelopId) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].type).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].type) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].skyldnerId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].skyldnerId) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].kravhaverId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].kravhaverId) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].mottakerId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].mottakerId) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].belop.toInt()).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].belop.toInt()) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].valutakode).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].valutakode) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].resultatkode).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].resultatkode) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].grunnlagReferanseListe.size).isEqualTo(3) },

      // Behandlingsreferanse
      Executable { assertThat(vedtakFunnet.behandlingsreferanseListe.size).isEqualTo(2) },
      Executable { assertThat(vedtakFunnet.behandlingsreferanseListe[0].kilde).isEqualTo(nyttVedtakRequest.behandlingsreferanseListe!![0].kilde) },
      Executable { assertThat(vedtakFunnet.behandlingsreferanseListe[0].referanse).isEqualTo(nyttVedtakRequest.behandlingsreferanseListe!![0].referanse) },

    )
  }
}
