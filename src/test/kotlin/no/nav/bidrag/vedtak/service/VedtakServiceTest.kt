package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.BidragVedtakTest
import no.nav.bidrag.vedtak.TestUtil.Companion.byggOppdaterVedtakMedMismatchEngangsbeløp
import no.nav.bidrag.vedtak.TestUtil.Companion.byggOppdaterVedtakMedMismatchPeriode
import no.nav.bidrag.vedtak.TestUtil.Companion.byggOppdaterVedtakMedMismatchStonadsendring
import no.nav.bidrag.vedtak.TestUtil.Companion.byggOppdaterVedtakMedMismatchVedtak
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtakRequest
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtakRequestUtenGrunnlag
import no.nav.bidrag.vedtak.exception.custom.GrunnlagsdataManglerException
import no.nav.bidrag.vedtak.exception.custom.VedtaksdataMatcherIkkeException
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
import org.assertj.core.api.Assertions.assertThatExceptionOfType
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
            Executable { assertThat(vedtakFunnet.opprettetTidspunkt).isNotNull() },
            Executable { assertThat(vedtakFunnet.opprettetAv).isEqualTo(nyttVedtakRequest.opprettetAv) },
            Executable { assertThat(vedtakFunnet.opprettetAvNavn).isEqualTo(nyttVedtakRequest.opprettetAvNavn) },
            Executable { assertThat(vedtakFunnet.enhetId).isEqualTo(nyttVedtakRequest.enhetId) },
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
            Executable { assertThat(vedtakFunnet.stonadsendringListe[0].endring).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].endring) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[0].omgjorVedtakId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].omgjorVedtakId) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[0].eksternReferanse).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].eksternReferanse) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe.size).isEqualTo(2) },

            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].type).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].type) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].sakId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].sakId) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].skyldnerId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].skyldnerId) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].kravhaverId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].kravhaverId) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].mottakerId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].mottakerId) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].indeksreguleringAar).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].indeksreguleringAar) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].innkreving).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].innkreving) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].endring).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].endring) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].omgjorVedtakId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].omgjorVedtakId) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].eksternReferanse).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].eksternReferanse) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe.size).isEqualTo(2) },

            // Periode
            Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].fomDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].fomDato) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].tilDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].tilDato) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].belop?.toInt()).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].belop?.toInt()) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].valutakode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].valutakode) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].resultatkode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].resultatkode) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].delytelseId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].delytelseId) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(3) },

            Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].fomDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].fomDato) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].tilDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].tilDato) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].belop?.toInt()).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].belop?.toInt()) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].valutakode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].valutakode) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].resultatkode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].resultatkode) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].delytelseId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].delytelseId) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(4) },

            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].fomDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].fomDato) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].tilDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].tilDato) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].belop?.toInt()).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].belop?.toInt()) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].valutakode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].valutakode) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].resultatkode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].resultatkode) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].delytelseId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].delytelseId) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(2) },

            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].fomDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].fomDato) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].tilDato).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].tilDato) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].belop?.toInt()).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].belop?.toInt()) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].valutakode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].valutakode) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].resultatkode).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].resultatkode) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].delytelseId).isEqualTo(nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].delytelseId) },
            Executable { assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(2) },

            // GrunnlagReferanse
            Executable {
                assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[0]
                )
            },
            Executable {
                assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[1]
                )
            },
            Executable {
                assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[2]).isEqualTo(
                    nyttVedtakRequest.stonadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[2]
                )
            },
            Executable {
                assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[0]
                )
            },
            Executable {
                assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[1]
                )
            },
            Executable {
                assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[2]).isEqualTo(
                    nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[2]
                )
            },
            Executable {
                assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[3]).isEqualTo(
                    nyttVedtakRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[3]
                )
            },

            Executable {
                assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].grunnlagReferanseListe[0]
                )
            },
            Executable {
                assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    nyttVedtakRequest.stonadsendringListe!![1].periodeListe[0].grunnlagReferanseListe[1]
                )
            },
            Executable {
                assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].grunnlagReferanseListe[0]
                )
            },
            Executable {
                assertThat(vedtakFunnet.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    nyttVedtakRequest.stonadsendringListe!![1].periodeListe[1].grunnlagReferanseListe[1]
                )
            },

            // Engangsbeløp
            Executable { assertThat(vedtakFunnet.engangsbelopListe.size).isEqualTo(2) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[0].type).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].type) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[0].sakId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].sakId) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[0].skyldnerId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].skyldnerId) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[0].kravhaverId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].kravhaverId) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[0].mottakerId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].mottakerId) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[0].belop?.toInt()).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].belop?.toInt()) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[0].valutakode).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].valutakode) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[0].resultatkode).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].resultatkode) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[0].innkreving).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].innkreving) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[0].endring).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].endring) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[0].omgjorVedtakId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].omgjorVedtakId) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[0].referanse).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].referanse) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[0].delytelseId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].delytelseId) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[0].eksternReferanse).isEqualTo(nyttVedtakRequest.engangsbelopListe!![0].eksternReferanse) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[0].grunnlagReferanseListe.size).isEqualTo(3) },

            Executable { assertThat(vedtakFunnet.engangsbelopListe[1].type).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].type) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[1].sakId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].sakId) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[1].skyldnerId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].skyldnerId) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[1].kravhaverId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].kravhaverId) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[1].mottakerId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].mottakerId) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[1].belop?.toInt()).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].belop?.toInt()) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[1].valutakode).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].valutakode) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[1].resultatkode).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].resultatkode) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[1].innkreving).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].innkreving) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[1].endring).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].endring) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[1].omgjorVedtakId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].omgjorVedtakId) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[1].referanse).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].referanse) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[1].delytelseId).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].delytelseId) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[1].eksternReferanse).isEqualTo(nyttVedtakRequest.engangsbelopListe!![1].eksternReferanse) },
            Executable { assertThat(vedtakFunnet.engangsbelopListe[1].grunnlagReferanseListe.size).isEqualTo(3) },

            // Behandlingsreferanse
            Executable { assertThat(vedtakFunnet.behandlingsreferanseListe.size).isEqualTo(2) },
            Executable { assertThat(vedtakFunnet.behandlingsreferanseListe[0].kilde).isEqualTo(nyttVedtakRequest.behandlingsreferanseListe!![0].kilde) },
            Executable { assertThat(vedtakFunnet.behandlingsreferanseListe[0].referanse).isEqualTo(nyttVedtakRequest.behandlingsreferanseListe!![0].referanse) }

        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal opprette vedtak uten grunnlag og så oppdatere vedtak med grunnlag`() {
        // Oppretter nytt vedtak
        val oopdaterVedtakUtenGrunnlagRequest = byggVedtakRequestUtenGrunnlag()
        val vedtakUtenGrunnlagVedtakId = vedtakService.opprettVedtak(oopdaterVedtakUtenGrunnlagRequest)

        // Henter vedtak uten grunnlag
        val vedtakUtenGrunnlag = vedtakService.hentVedtak(vedtakUtenGrunnlagVedtakId)

        val oopdaterVedtakMedGrunnlagRequest = byggVedtakRequest()

        vedtakService.oppdaterVedtak(vedtakUtenGrunnlagVedtakId, oopdaterVedtakMedGrunnlagRequest)

        // Henter oppdatert vedtak
        val oppdatertVedtakMedGrunnlag = vedtakService.hentVedtak(vedtakUtenGrunnlagVedtakId)

        assertAll(

            // Grunnlag
            Executable { assertThat(vedtakUtenGrunnlag.grunnlagListe).isEmpty() },

            Executable { assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[0].referanse).isEqualTo(oopdaterVedtakMedGrunnlagRequest.grunnlagListe[0].referanse) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[0].type).isEqualTo(oopdaterVedtakMedGrunnlagRequest.grunnlagListe[0].type) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[0].innhold).isEqualTo(oopdaterVedtakMedGrunnlagRequest.grunnlagListe[0].innhold) },

            Executable { assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[1].referanse).isEqualTo(oopdaterVedtakMedGrunnlagRequest.grunnlagListe[1].referanse) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[1].type).isEqualTo(oopdaterVedtakMedGrunnlagRequest.grunnlagListe[1].type) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[1].innhold).isEqualTo(oopdaterVedtakMedGrunnlagRequest.grunnlagListe[1].innhold) },

            Executable { assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[2].referanse).isEqualTo(oopdaterVedtakMedGrunnlagRequest.grunnlagListe[2].referanse) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[2].type).isEqualTo(oopdaterVedtakMedGrunnlagRequest.grunnlagListe[2].type) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[2].innhold).isEqualTo(oopdaterVedtakMedGrunnlagRequest.grunnlagListe[2].innhold) },

            Executable { assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[3].referanse).isEqualTo(oopdaterVedtakMedGrunnlagRequest.grunnlagListe[3].referanse) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[3].type).isEqualTo(oopdaterVedtakMedGrunnlagRequest.grunnlagListe[3].type) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[3].innhold).isEqualTo(oopdaterVedtakMedGrunnlagRequest.grunnlagListe[3].innhold) },

            Executable { assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe.size).isEqualTo(4) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe.size).isEqualTo(2) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.behandlingsreferanseListe.size).isEqualTo(2) },

            // Periode
            Executable { assertThat(vedtakUtenGrunnlag.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe).isEmpty() },
            Executable { assertThat(vedtakUtenGrunnlag.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe).isEmpty() },
            Executable { assertThat(vedtakUtenGrunnlag.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe).isEmpty() },
            Executable { assertThat(vedtakUtenGrunnlag.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe).isEmpty() },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(3) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(4) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(2) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(2) },

            // GrunnlagReferanse
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stonadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[0]
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stonadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[1]
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[2]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stonadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[2]
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[0]
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[1]
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[2]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[2]
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[3]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[3]
                )
            },

            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stonadsendringListe!![1].periodeListe[0].grunnlagReferanseListe[0]
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stonadsendringListe!![1].periodeListe[0].grunnlagReferanseListe[1]
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stonadsendringListe!![1].periodeListe[1].grunnlagReferanseListe[0]
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stonadsendringListe!![1].periodeListe[1].grunnlagReferanseListe[1]
                )
            },

            // Engangsbeløp
            Executable { assertThat(oppdatertVedtakMedGrunnlag.engangsbelopListe.size).isEqualTo(2) },

            Executable { assertThat(vedtakUtenGrunnlag.engangsbelopListe[0].grunnlagReferanseListe).isEmpty() },
            Executable { assertThat(vedtakUtenGrunnlag.engangsbelopListe[1].grunnlagReferanseListe).isEmpty() },

            Executable { assertThat(oppdatertVedtakMedGrunnlag.engangsbelopListe[0].grunnlagReferanseListe.size).isEqualTo(3) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.engangsbelopListe[1].grunnlagReferanseListe.size).isEqualTo(3) }

        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `sjekk på at eventuelt eksisterende grunnlag på vedtak slettes før oppdatering av vedtak`() {
        // Oppretter nytt vedtak
        val vedtakRequest = byggVedtakRequest()
        val vedtakId = vedtakService.opprettVedtak(vedtakRequest)

        // Henter vedtak uten grunnlag
        val vedtak = vedtakService.hentVedtak(vedtakId)

        val oppdaterVedtakMedGrunnlagRequest = byggVedtakRequest()

        vedtakService.oppdaterVedtak(vedtakId, oppdaterVedtakMedGrunnlagRequest)

        // Henter oppdatert vedtak
        val oppdatertVedtakMedGrunnlag = vedtakService.hentVedtak(vedtakId)

        assertAll(

            // Grunnlag
            Executable { assertThat(vedtak.grunnlagListe.size).isEqualTo(4) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe.size).isEqualTo(4) },

            // Periode
            Executable { assertThat(vedtak.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(3) },
            Executable { assertThat(vedtak.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(4) },
            Executable { assertThat(vedtak.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(2) },
            Executable { assertThat(vedtak.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(2) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(3) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(4) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(2) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(2) },

            // GrunnlagReferanse
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stonadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[0]
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stonadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[1]
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[2]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stonadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[2]
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[0]
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[1]
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[2]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[2]
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[0].periodeListe[1].grunnlagReferanseListe[3]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stonadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[3]
                )
            },

            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stonadsendringListe!![1].periodeListe[0].grunnlagReferanseListe[0]
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[1].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stonadsendringListe!![1].periodeListe[0].grunnlagReferanseListe[1]
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stonadsendringListe!![1].periodeListe[1].grunnlagReferanseListe[0]
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stonadsendringListe[1].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stonadsendringListe!![1].periodeListe[1].grunnlagReferanseListe[1]
                )
            },

            // Engangsbeløp
            Executable { assertThat(oppdatertVedtakMedGrunnlag.engangsbelopListe.size).isEqualTo(2) },

            Executable { assertThat(vedtak.engangsbelopListe[0].grunnlagReferanseListe.size).isEqualTo(3) },
            Executable { assertThat(vedtak.engangsbelopListe[1].grunnlagReferanseListe.size).isEqualTo(3) },

            Executable { assertThat(oppdatertVedtakMedGrunnlag.engangsbelopListe[0].grunnlagReferanseListe.size).isEqualTo(3) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.engangsbelopListe[1].grunnlagReferanseListe.size).isEqualTo(3) }

        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `test at oppdatering av vedtak med mismatch på vedtak feiler`() {
        // Oppretter nytt vedtak
        val vedtak = byggVedtakRequest()
        val vedtakId = vedtakService.opprettVedtak(vedtak)

        val oppdaterVedtakMedGrunnlagRequest = byggOppdaterVedtakMedMismatchVedtak()

        assertThatExceptionOfType(VedtaksdataMatcherIkkeException::class.java).isThrownBy {
            vedtakService.oppdaterVedtak(vedtakId, oppdaterVedtakMedGrunnlagRequest)
        }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `test at oppdatering av vedtak med mismatch på stonadsendring feiler`() {
        // Oppretter nytt vedtak
        val vedtak = byggVedtakRequest()
        val vedtakId = vedtakService.opprettVedtak(vedtak)

        val oppdaterVedtakMedGrunnlagRequest = byggOppdaterVedtakMedMismatchStonadsendring()

        assertThatExceptionOfType(VedtaksdataMatcherIkkeException::class.java).isThrownBy {
            vedtakService.oppdaterVedtak(vedtakId, oppdaterVedtakMedGrunnlagRequest)
        }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `test at oppdatering av vedtak med mismatch på periode feiler`() {
        // Oppretter nytt vedtak
        val vedtak = byggVedtakRequest()
        val vedtakId = vedtakService.opprettVedtak(vedtak)

        val oppdaterVedtakMedGrunnlagRequest = byggOppdaterVedtakMedMismatchPeriode()

        assertThatExceptionOfType(VedtaksdataMatcherIkkeException::class.java).isThrownBy {
            vedtakService.oppdaterVedtak(vedtakId, oppdaterVedtakMedGrunnlagRequest)
        }
    }


    @Test
    @Suppress("NonAsciiCharacters")
    fun `test at oppdatering av vedtak med mismatch på engangsbeløp feiler`() {
        // Oppretter nytt vedtak
        val vedtak = byggVedtakRequest()
        val vedtakId = vedtakService.opprettVedtak(vedtak)

        val oppdaterVedtakMedGrunnlagRequest = byggOppdaterVedtakMedMismatchEngangsbeløp()

        assertThatExceptionOfType(VedtaksdataMatcherIkkeException::class.java).isThrownBy {
            vedtakService.oppdaterVedtak(vedtakId, oppdaterVedtakMedGrunnlagRequest)
        }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `test at oppdatering av vedtak feiler hvis grunnlag mangler i request`() {
        // Oppretter nytt vedtak
        val vedtak = byggVedtakRequest()
        val vedtakId = vedtakService.opprettVedtak(vedtak)

        val oppdaterVedtakMedGrunnlagRequest = byggVedtakRequestUtenGrunnlag()

        assertThatExceptionOfType(GrunnlagsdataManglerException::class.java).isThrownBy {
            vedtakService.oppdaterVedtak(vedtakId, oppdaterVedtakMedGrunnlagRequest)
        }
    }
}
