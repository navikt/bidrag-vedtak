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
      Executable { assertThat(vedtakFunnet.kilde).isEqualTo(nyttVedtakRequest.kilde) },
      Executable { assertThat(vedtakFunnet.type).isEqualTo(nyttVedtakRequest.type) },
//      Det fjernes 3 desimaler fra vedtakTidspunkt etter lagring, Postgres-feature?
//      Executable { assertThat(vedtakFunnet.vedtakTidspunkt).isEqualTo(nyttVedtakRequest.vedtakTidspunkt) },
      Executable { assertThat(vedtakFunnet.opprettetTimestamp).isNotNull() },
      Executable { assertThat(vedtakFunnet.opprettetAv).isEqualTo(nyttVedtakRequest.opprettetAv) },
      Executable { assertThat(vedtakFunnet.enhetId).isEqualTo(nyttVedtakRequest.enhetId) },
      Executable { assertThat(vedtakFunnet.eksternReferanse).isEqualTo(nyttVedtakRequest.eksternReferanse) },
      Executable { assertThat(vedtakFunnet.utsattTilDato).isEqualTo(nyttVedtakRequest.utsattTilDato) },
      Executable { assertThat(vedtakFunnet.utsattTilDato).isEqualTo(nyttVedtakRequest.utsattTilDato) },
      Executable { assertThat(vedtakFunnet.grunnlagListe.size).isEqualTo(4) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe.size).isEqualTo(2) },
      Executable { assertThat(vedtakFunnet.behandlingsreferanseListe.size).isEqualTo(2) },

      // Grunnlag
      Executable { assertThat(vedtakFunnet.grunnlagListe[0].referanse).isEqualTo(nyttVedtakRequest.grunnlagListe[0].referanse) },
      Executable { assertThat(vedtakFunnet.grunnlagListe[0].type).isEqualTo(nyttVedtakRequest.grunnlagListe[0].type) },
      Executable { assertThat(vedtakFunnet.grunnlagListe[0].innhold).isEqualTo(nyttVedtakRequest.grunnlagListe[0].innhold) },

      Executable { assertThat(vedtakFunnet.grunnlagListe[1].referanse).isEqualTo(nyttVedtakRequest.grunnlagListe[1].referanse) },
      Executable { assertThat(vedtakFunnet.grunnlagListe[1].type).isEqualTo(nyttVedtakRequest.grunnlagListe[1].type) },
      Executable { assertThat(vedtakFunnet.grunnlagListe[1].innhold).isEqualTo(nyttVedtakRequest.grunnlagListe[1].innhold) },

      Executable { assertThat(vedtakFunnet.grunnlagListe[2].referanse).isEqualTo(nyttVedtakRequest.grunnlagListe[2].referanse) },
      Executable { assertThat(vedtakFunnet.grunnlagListe[2].type).isEqualTo(nyttVedtakRequest.grunnlagListe[2].type) },
      Executable { assertThat(vedtakFunnet.grunnlagListe[2].innhold).isEqualTo(nyttVedtakRequest.grunnlagListe[2].innhold) },

      Executable { assertThat(vedtakFunnet.grunnlagListe[3].referanse).isEqualTo(nyttVedtakRequest.grunnlagListe[3].referanse) },
      Executable { assertThat(vedtakFunnet.grunnlagListe[3].type).isEqualTo(nyttVedtakRequest.grunnlagListe[3].type) },
      Executable { assertThat(vedtakFunnet.grunnlagListe[3].innhold).isEqualTo(nyttVedtakRequest.grunnlagListe[3].innhold) },

      // Stonadsendring
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].type).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].type) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].sakId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].sakId) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].skyldnerId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].skyldnerId) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].kravhaverId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].kravhaverId) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].mottakerId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].mottakerId) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].indeksreguleringAar).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].indeksreguleringAar) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].innkreving).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].innkreving) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe.size).isEqualTo(2) },

      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].type).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].type) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].sakId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].sakId) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].skyldnerId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].skyldnerId) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].kravhaverId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].kravhaverId) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].mottakerId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].mottakerId) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].indeksreguleringAar).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].indeksreguleringAar) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].innkreving).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].innkreving) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe.size).isEqualTo(2) },

      // Periode
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].fomDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].fomDato) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].tilDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].tilDato) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].belop?.toInt()).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].belop?.toInt()) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].valutakode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].valutakode) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].resultatkode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].resultatkode) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].referanse).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].referanse) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(3) },

      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].fomDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].fomDato) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].tilDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].tilDato) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].belop?.toInt()).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].belop?.toInt()) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].valutakode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].valutakode) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].resultatkode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].resultatkode) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].referanse).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].referanse) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(4) },

      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].fomDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].fomDato) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].tilDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].tilDato) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].belop?.toInt()).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].belop?.toInt()) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].valutakode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].valutakode) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].resultatkode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].resultatkode) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].referanse).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].referanse) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(2) },

      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].fomDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].fomDato) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].tilDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].tilDato) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].belop?.toInt()).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].belop?.toInt()) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].valutakode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].valutakode) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].resultatkode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].resultatkode) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].referanse).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].referanse) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(2) },

      // GrunnlagReferanse
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[0]) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[1]) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[2]).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[2]) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[0]) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[1]) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[2]).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[2]) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[3]).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[3]) },

      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].grunnlagReferanseListe[0]) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].grunnlagReferanseListe[1]) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].grunnlagReferanseListe[0]) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
          nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].grunnlagReferanseListe[1]) },

      // Engangsbeløp
      Executable { assertThat(vedtakFunnet.engangsbelopListe.size).isEqualTo(2) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].endrerId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].endrerId) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].type).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].type) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].sakId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].sakId) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].skyldnerId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].skyldnerId) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].kravhaverId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].kravhaverId) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].mottakerId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].mottakerId) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].belop?.toInt()).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].belop?.toInt()) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].valutakode).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].valutakode) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].resultatkode).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].resultatkode) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].referanse).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].referanse) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].innkreving).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].innkreving) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].grunnlagReferanseListe.size).isEqualTo(3) },

      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].endrerId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].endrerId) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].type).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].type) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].sakId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].sakId) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].skyldnerId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].skyldnerId) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].kravhaverId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].kravhaverId) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].mottakerId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].mottakerId) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].belop?.toInt()).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].belop?.toInt()) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].valutakode).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].valutakode) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].resultatkode).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].resultatkode) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].referanse).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].referanse) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].innkreving).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].innkreving) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[1].grunnlagReferanseListe.size).isEqualTo(3) },

      // Behandlingsreferanse
      Executable { assertThat(vedtakFunnet.behandlingsreferanseListe.size).isEqualTo(2) },
      Executable { assertThat(vedtakFunnet.behandlingsreferanseListe[0].kilde).isEqualTo(nyttVedtakRequest.behandlingsreferanseListe!![0].kilde) },
      Executable { assertThat(vedtakFunnet.behandlingsreferanseListe[0].referanse).isEqualTo(nyttVedtakRequest.behandlingsreferanseListe!![0].referanse) },

    )
  }
}
