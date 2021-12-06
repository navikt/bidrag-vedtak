package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.TestUtil.Companion.byggKomplettVedtakRequest
import no.nav.bidrag.vedtak.persistence.repository.BehandlingsreferanseRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbelopGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbelopRepository
import no.nav.bidrag.vedtak.persistence.repository.GrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeRepository
import no.nav.bidrag.vedtak.persistence.repository.StonadsendringRepository
import no.nav.bidrag.vedtak.persistence.repository.VedtakRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@DisplayName("VedtakServiceTest")
@ActiveProfiles(BidragVedtakLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

  @Autowired
  private lateinit var persistenceService: PersistenceService

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
  fun `skal opprette og hente komplett vedtak`() {
    // Oppretter nytt komplett vedtak
    val nyttKomplettVedtakRequest = byggKomplettVedtakRequest()
    val nyttKomplettVedtakOpprettet = vedtakService.opprettKomplettVedtak(nyttKomplettVedtakRequest)

    assertAll(
      Executable { assertThat(nyttKomplettVedtakOpprettet).isNotNull() }
    )

    // Henter komplett vedtak
    val komplettVedtakFunnet = vedtakService.hentKomplettVedtak(nyttKomplettVedtakOpprettet)

    assertAll(
      Executable { assertThat(komplettVedtakFunnet).isNotNull() },

      // Vedtak
      Executable { assertThat(komplettVedtakFunnet.vedtakId).isNotNull() },
      Executable { assertThat(komplettVedtakFunnet.opprettetTimestamp).isNotNull() },
      Executable { assertThat(komplettVedtakFunnet.saksbehandlerId).isEqualTo(nyttKomplettVedtakRequest.saksbehandlerId) },
      Executable { assertThat(komplettVedtakFunnet.vedtakDato).isEqualTo(nyttKomplettVedtakRequest.vedtakDato) },
      Executable { assertThat(komplettVedtakFunnet.enhetId).isEqualTo(nyttKomplettVedtakRequest.enhetId) },
      Executable { assertThat(komplettVedtakFunnet.grunnlagListe.size).isEqualTo(4) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe.size).isEqualTo(2) },
      Executable { assertThat(komplettVedtakFunnet.behandlingsreferanseListe.size).isEqualTo(2) },

      // Grunnlag
      Executable { assertThat(komplettVedtakFunnet.grunnlagListe[0].grunnlagReferanse).isEqualTo(nyttKomplettVedtakRequest.grunnlagListe[0].grunnlagReferanse) },
      Executable { assertThat(komplettVedtakFunnet.grunnlagListe[0].grunnlagType).isEqualTo(nyttKomplettVedtakRequest.grunnlagListe[0].grunnlagType) },
      Executable { assertThat(komplettVedtakFunnet.grunnlagListe[0].grunnlagInnhold).isEqualTo(nyttKomplettVedtakRequest.grunnlagListe[0].grunnlagInnhold.toString()) },

      Executable { assertThat(komplettVedtakFunnet.grunnlagListe[1].grunnlagReferanse).isEqualTo(nyttKomplettVedtakRequest.grunnlagListe[1].grunnlagReferanse) },
      Executable { assertThat(komplettVedtakFunnet.grunnlagListe[1].grunnlagType).isEqualTo(nyttKomplettVedtakRequest.grunnlagListe[1].grunnlagType) },
      Executable { assertThat(komplettVedtakFunnet.grunnlagListe[1].grunnlagInnhold).isEqualTo(nyttKomplettVedtakRequest.grunnlagListe[1].grunnlagInnhold.toString()) },

      Executable { assertThat(komplettVedtakFunnet.grunnlagListe[2].grunnlagReferanse).isEqualTo(nyttKomplettVedtakRequest.grunnlagListe[2].grunnlagReferanse) },
      Executable { assertThat(komplettVedtakFunnet.grunnlagListe[2].grunnlagType).isEqualTo(nyttKomplettVedtakRequest.grunnlagListe[2].grunnlagType) },
      Executable { assertThat(komplettVedtakFunnet.grunnlagListe[2].grunnlagInnhold).isEqualTo(nyttKomplettVedtakRequest.grunnlagListe[2].grunnlagInnhold.toString()) },

      Executable { assertThat(komplettVedtakFunnet.grunnlagListe[3].grunnlagReferanse).isEqualTo(nyttKomplettVedtakRequest.grunnlagListe[3].grunnlagReferanse) },
      Executable { assertThat(komplettVedtakFunnet.grunnlagListe[3].grunnlagType).isEqualTo(nyttKomplettVedtakRequest.grunnlagListe[3].grunnlagType) },
      Executable { assertThat(komplettVedtakFunnet.grunnlagListe[3].grunnlagInnhold).isEqualTo(nyttKomplettVedtakRequest.grunnlagListe[3].grunnlagInnhold.toString()) },

      // Stonadsendring
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].stonadType).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[0].stonadType) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].sakId).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[0].sakId) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].behandlingId).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[0].behandlingId) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].skyldnerId).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[0].skyldnerId) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].kravhaverId).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[0].kravhaverId) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].mottakerId).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[0].mottakerId) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe.size).isEqualTo(2) },

      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].stonadType).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[1].stonadType) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].sakId).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[1].sakId) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].behandlingId).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[1].behandlingId) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].skyldnerId).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[1].skyldnerId) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].kravhaverId).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[1].kravhaverId) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].mottakerId).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[1].mottakerId) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].periodeListe.size).isEqualTo(2) },

      // Periode
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[0].periodeFomDato).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[0].periodeListe[0].periodeFomDato) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[0].periodeTilDato).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[0].periodeListe[0].periodeTilDato) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[0].belop.toInt()).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[0].periodeListe[0].belop.toInt()) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[0].valutakode).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[0].periodeListe[0].valutakode) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[0].resultatkode).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[0].periodeListe[0].resultatkode) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(3) },

      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[1].periodeFomDato).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[0].periodeListe[1].periodeFomDato) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[1].periodeTilDato).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[0].periodeListe[1].periodeTilDato) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[1].belop.toInt()).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[0].periodeListe[1].belop.toInt()) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[1].valutakode).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[0].periodeListe[1].valutakode) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[1].resultatkode).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[0].periodeListe[1].resultatkode) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(4) },

      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].periodeListe[0].periodeFomDato).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[1].periodeListe[0].periodeFomDato) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].periodeListe[0].periodeTilDato).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[1].periodeListe[0].periodeTilDato) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].periodeListe[0].belop.toInt()).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[1].periodeListe[0].belop.toInt()) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].periodeListe[0].valutakode).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[1].periodeListe[0].valutakode) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].periodeListe[0].resultatkode).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[1].periodeListe[0].resultatkode) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(2) },

      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].periodeListe[1].periodeFomDato).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[1].periodeListe[1].periodeFomDato) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].periodeListe[1].periodeTilDato).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[1].periodeListe[1].periodeTilDato) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].periodeListe[1].belop.toInt()).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[1].periodeListe[1].belop.toInt()) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].periodeListe[1].valutakode).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[1].periodeListe[1].valutakode) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].periodeListe[1].resultatkode).isEqualTo(nyttKomplettVedtakRequest.stonadsendringListe[1].periodeListe[1].resultatkode) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(2) },

      // GrunnlagReferanse
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0].grunnlagReferanse).isEqualTo(
          nyttKomplettVedtakRequest.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0].grunnlagReferanse) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[1].grunnlagReferanse).isEqualTo(
          nyttKomplettVedtakRequest.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[1].grunnlagReferanse) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[2].grunnlagReferanse).isEqualTo(
          nyttKomplettVedtakRequest.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[2].grunnlagReferanse) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[0].grunnlagReferanse).isEqualTo(
          nyttKomplettVedtakRequest.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[0].grunnlagReferanse) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[1].grunnlagReferanse).isEqualTo(
          nyttKomplettVedtakRequest.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[1].grunnlagReferanse) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[2].grunnlagReferanse).isEqualTo(
          nyttKomplettVedtakRequest.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[2].grunnlagReferanse) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[3].grunnlagReferanse).isEqualTo(
          nyttKomplettVedtakRequest.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[3].grunnlagReferanse) },

      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe[0].grunnlagReferanse).isEqualTo(
          nyttKomplettVedtakRequest.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe[0].grunnlagReferanse) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe[1].grunnlagReferanse).isEqualTo(
          nyttKomplettVedtakRequest.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe[1].grunnlagReferanse) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe[0].grunnlagReferanse).isEqualTo(
          nyttKomplettVedtakRequest.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe[0].grunnlagReferanse) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe[1].grunnlagReferanse).isEqualTo(
          nyttKomplettVedtakRequest.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe[1].grunnlagReferanse) },

      // Engangsbeløp
      Executable { assertThat(komplettVedtakFunnet.engangsbelopListe.size).isEqualTo(2) },
      Executable { assertThat(komplettVedtakFunnet.engangsbelopListe[0].endrerEngangsbelopId).isEqualTo(nyttKomplettVedtakRequest.engangsbelopListe[0].endrerEngangsbelopId) },
      Executable { assertThat(komplettVedtakFunnet.engangsbelopListe[0].type).isEqualTo(nyttKomplettVedtakRequest.engangsbelopListe[0].type) },
      Executable { assertThat(komplettVedtakFunnet.engangsbelopListe[0].skyldnerId).isEqualTo(nyttKomplettVedtakRequest.engangsbelopListe[0].skyldnerId) },
      Executable { assertThat(komplettVedtakFunnet.engangsbelopListe[0].kravhaverId).isEqualTo(nyttKomplettVedtakRequest.engangsbelopListe[0].kravhaverId) },
      Executable { assertThat(komplettVedtakFunnet.engangsbelopListe[0].mottakerId).isEqualTo(nyttKomplettVedtakRequest.engangsbelopListe[0].mottakerId) },
      Executable { assertThat(komplettVedtakFunnet.engangsbelopListe[0].belop.toInt()).isEqualTo(nyttKomplettVedtakRequest.engangsbelopListe[0].belop.toInt()) },
      Executable { assertThat(komplettVedtakFunnet.engangsbelopListe[0].valutakode).isEqualTo(nyttKomplettVedtakRequest.engangsbelopListe[0].valutakode) },
      Executable { assertThat(komplettVedtakFunnet.engangsbelopListe[0].resultatkode).isEqualTo(nyttKomplettVedtakRequest.engangsbelopListe[0].resultatkode) },
      Executable { assertThat(komplettVedtakFunnet.engangsbelopListe[0].grunnlagReferanseListe.size).isEqualTo(3) },

      Executable { assertThat(komplettVedtakFunnet.engangsbelopListe[1].endrerEngangsbelopId).isEqualTo(nyttKomplettVedtakRequest.engangsbelopListe[1].endrerEngangsbelopId) },
      Executable { assertThat(komplettVedtakFunnet.engangsbelopListe[1].type).isEqualTo(nyttKomplettVedtakRequest.engangsbelopListe[1].type) },
      Executable { assertThat(komplettVedtakFunnet.engangsbelopListe[1].skyldnerId).isEqualTo(nyttKomplettVedtakRequest.engangsbelopListe[1].skyldnerId) },
      Executable { assertThat(komplettVedtakFunnet.engangsbelopListe[1].kravhaverId).isEqualTo(nyttKomplettVedtakRequest.engangsbelopListe[1].kravhaverId) },
      Executable { assertThat(komplettVedtakFunnet.engangsbelopListe[1].mottakerId).isEqualTo(nyttKomplettVedtakRequest.engangsbelopListe[1].mottakerId) },
      Executable { assertThat(komplettVedtakFunnet.engangsbelopListe[1].belop.toInt()).isEqualTo(nyttKomplettVedtakRequest.engangsbelopListe[1].belop.toInt()) },
      Executable { assertThat(komplettVedtakFunnet.engangsbelopListe[1].valutakode).isEqualTo(nyttKomplettVedtakRequest.engangsbelopListe[1].valutakode) },
      Executable { assertThat(komplettVedtakFunnet.engangsbelopListe[1].resultatkode).isEqualTo(nyttKomplettVedtakRequest.engangsbelopListe[1].resultatkode) },
      Executable { assertThat(komplettVedtakFunnet.engangsbelopListe[1].grunnlagReferanseListe.size).isEqualTo(3) },

      // Behandlingsreferanse
      Executable { assertThat(komplettVedtakFunnet.behandlingsreferanseListe.size).isEqualTo(2) },
      Executable { assertThat(komplettVedtakFunnet.behandlingsreferanseListe[0].kilde).isEqualTo(nyttKomplettVedtakRequest.behandlingsreferanseListe[0].kilde) },
      Executable { assertThat(komplettVedtakFunnet.behandlingsreferanseListe[0].referanse).isEqualTo(nyttKomplettVedtakRequest.behandlingsreferanseListe[0].referanse) },

    )
  }
}
