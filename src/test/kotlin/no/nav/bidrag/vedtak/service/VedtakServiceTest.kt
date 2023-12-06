package no.nav.bidrag.vedtak.service

import no.nav.bidrag.transport.behandling.vedtak.request.OpprettEngangsbeløpRequestDto
import no.nav.bidrag.vedtak.BidragVedtakTest
import no.nav.bidrag.vedtak.TestUtil.Companion.byggOppdaterVedtakMedMismatchEngangsbeløp
import no.nav.bidrag.vedtak.TestUtil.Companion.byggOppdaterVedtakMedMismatchPeriode
import no.nav.bidrag.vedtak.TestUtil.Companion.byggOppdaterVedtakMedMismatchStønadsendring
import no.nav.bidrag.vedtak.TestUtil.Companion.byggOppdaterVedtakMedMismatchVedtak
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtakMedDuplikateReferanserRequest
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtakRequest
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtakRequestUtenGrunnlag
import no.nav.bidrag.vedtak.exception.custom.GrunnlagsdataManglerException
import no.nav.bidrag.vedtak.exception.custom.VedtaksdataMatcherIkkeException
import no.nav.bidrag.vedtak.persistence.repository.BehandlingsreferanseRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbeløpGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbeløpRepository
import no.nav.bidrag.vedtak.persistence.repository.GrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeRepository
import no.nav.bidrag.vedtak.persistence.repository.StønadsendringGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.StønadsendringRepository
import no.nav.bidrag.vedtak.persistence.repository.VedtakRepository
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.HttpClientErrorException

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
    private lateinit var engangsbeløpGrunnlagRepository: EngangsbeløpGrunnlagRepository

    @Autowired
    private lateinit var periodeGrunnlagRepository: PeriodeGrunnlagRepository

    @Autowired
    private lateinit var grunnlagRepository: GrunnlagRepository

    @Autowired
    private lateinit var engangsbeløpRepository: EngangsbeløpRepository

    @Autowired
    private lateinit var periodeRepository: PeriodeRepository

    @Autowired
    private lateinit var stønadsendringGrunnlagRepository: StønadsendringGrunnlagRepository

    @Autowired
    private lateinit var stønadsendringRepository: StønadsendringRepository

    @Autowired
    private lateinit var vedtakRepository: VedtakRepository

    @BeforeEach
    fun `init`() {
        // Sletter alle forekomster
        behandlingsreferanseRepository.deleteAll()
        engangsbeløpGrunnlagRepository.deleteAll()
        periodeGrunnlagRepository.deleteAll()
        stønadsendringGrunnlagRepository.deleteAll()
        engangsbeløpRepository.deleteAll()
        grunnlagRepository.deleteAll()
        periodeRepository.deleteAll()
        stønadsendringRepository.deleteAll()
        vedtakRepository.deleteAll()
    }

    @Test
    fun `skal opprette og hente vedtak`() {
        // Oppretter nytt vedtak
        val nyttVedtakRequest = byggVedtakRequest()
        val nyttVedtakOpprettet = vedtakService.opprettVedtak(nyttVedtakRequest).vedtaksid

        assertAll(
            Executable { assertThat(nyttVedtakOpprettet).isNotNull() },
        )

        // Henter vedtak
        val vedtakFunnet = vedtakService.hentVedtak(nyttVedtakOpprettet)

        assertAll(
            Executable { assertThat(vedtakFunnet).isNotNull() },

            // Vedtak
            Executable { assertThat(vedtakFunnet.kilde).isEqualTo(nyttVedtakRequest.kilde) },
            Executable { assertThat(vedtakFunnet.type).isEqualTo(nyttVedtakRequest.type) },
//      Det fjernes 3 desimaler fra vedtakstidspunkt etter lagring, Postgres-feature?
//      Executable { assertThat(vedtakFunnet.vedtakstidspunkt).isEqualTo(nyttVedtakRequest.vedtakstidspunkt) },
            Executable { assertThat(vedtakFunnet.opprettetTidspunkt).isNotNull() },
            Executable { assertThat(vedtakFunnet.opprettetAv).isEqualTo(nyttVedtakRequest.opprettetAv) },
            Executable { assertThat(vedtakFunnet.enhetsnummer).isEqualTo(nyttVedtakRequest.enhetsnummer) },
            Executable { assertThat(vedtakFunnet.innkrevingUtsattTilDato).isEqualTo(nyttVedtakRequest.innkrevingUtsattTilDato) },
            Executable { assertThat(vedtakFunnet.fastsattILand).isEqualTo(nyttVedtakRequest.fastsattILand) },
            Executable { assertThat(vedtakFunnet.grunnlagListe.size).isEqualTo(8) },
            Executable { assertThat(vedtakFunnet.stønadsendringListe.size).isEqualTo(2) },
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

            Executable { assertThat(vedtakFunnet.grunnlagListe[4].referanse).isEqualTo(nyttVedtakRequest.grunnlagListe[4].referanse) },
            Executable { assertThat(vedtakFunnet.grunnlagListe[4].type).isEqualTo(nyttVedtakRequest.grunnlagListe[4].type) },
            Executable { assertThat(vedtakFunnet.grunnlagListe[4].innhold).isEqualTo(nyttVedtakRequest.grunnlagListe[4].innhold) },

            Executable { assertThat(vedtakFunnet.grunnlagListe[5].referanse).isEqualTo(nyttVedtakRequest.grunnlagListe[5].referanse) },
            Executable { assertThat(vedtakFunnet.grunnlagListe[5].type).isEqualTo(nyttVedtakRequest.grunnlagListe[5].type) },
            Executable { assertThat(vedtakFunnet.grunnlagListe[5].innhold).isEqualTo(nyttVedtakRequest.grunnlagListe[5].innhold) },

            // Stønadsendring
            Executable { assertThat(vedtakFunnet.stønadsendringListe[0].type).isEqualTo(nyttVedtakRequest.stønadsendringListe!![0].type) },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[0].sak).isEqualTo(nyttVedtakRequest.stønadsendringListe!![0].sak) },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].skyldner.toString()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].skyldner.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].kravhaver.toString()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].kravhaver.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].mottaker.toString()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].mottaker.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].førsteIndeksreguleringsår).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].førsteIndeksreguleringsår,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].innkreving)
                    .isEqualTo(nyttVedtakRequest.stønadsendringListe!![0].innkreving)
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].beslutning)
                    .isEqualTo(nyttVedtakRequest.stønadsendringListe!![0].beslutning)
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].omgjørVedtakId).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].omgjørVedtakId,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].eksternReferanse).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].eksternReferanse,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].grunnlagReferanseListe[1],
                )
            },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[0].grunnlagReferanseListe.size).isEqualTo(2) },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe.size).isEqualTo(2) },

            Executable { assertThat(vedtakFunnet.stønadsendringListe[1].type).isEqualTo(nyttVedtakRequest.stønadsendringListe!![1].type) },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[1].sak).isEqualTo(nyttVedtakRequest.stønadsendringListe!![1].sak) },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].skyldner.toString()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].skyldner.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].kravhaver.toString()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].kravhaver.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].mottaker.toString()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].mottaker.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].førsteIndeksreguleringsår).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].førsteIndeksreguleringsår,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].innkreving)
                    .isEqualTo(nyttVedtakRequest.stønadsendringListe!![1].innkreving)
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].beslutning)
                    .isEqualTo(nyttVedtakRequest.stønadsendringListe!![1].beslutning)
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].omgjørVedtakId).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].omgjørVedtakId,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].eksternReferanse).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].eksternReferanse,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].grunnlagReferanseListe[1],
                )
            },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe.size).isEqualTo(2) },

            // Periode
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].periode.fom).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].periodeListe[0].periode.fom,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].periode.til).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].periodeListe[0].periode.til,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].beløp?.toInt()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].periodeListe[0].beløp?.toInt(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].valutakode).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].periodeListe[0].valutakode,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].resultatkode).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].periodeListe[0].resultatkode,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].delytelseId).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].periodeListe[0].delytelseId,
                )
            },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(3) },

            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].periode.fom).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].periodeListe[1].periode.fom,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].periode.til).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].periodeListe[1].periode.til,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].beløp?.toInt()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].periodeListe[1].beløp?.toInt(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].valutakode).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].periodeListe[1].valutakode,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].resultatkode).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].periodeListe[1].resultatkode,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].delytelseId).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].periodeListe[1].delytelseId,
                )
            },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(4) },

            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[0].periode.fom).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].periodeListe[0].periode.fom,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[0].periode.til).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].periodeListe[0].periode.til,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[0].beløp?.toInt()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].periodeListe[0].beløp?.toInt(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[0].valutakode).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].periodeListe[0].valutakode,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[0].resultatkode).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].periodeListe[0].resultatkode,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[0].delytelseId).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].periodeListe[0].delytelseId,
                )
            },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(2) },

            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[1].periode.fom).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].periodeListe[1].periode.fom,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[1].periode.til).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].periodeListe[1].periode.til,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[1].beløp?.toInt()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].periodeListe[1].beløp?.toInt(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[1].valutakode).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].periodeListe[1].valutakode,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[1].resultatkode).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].periodeListe[1].resultatkode,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[1].delytelseId).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].periodeListe[1].delytelseId,
                )
            },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(2) },

            // GrunnlagReferanse
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[1],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[2]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[2],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[1],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[2]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[2],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[3]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[3],
                )
            },

            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].periodeListe[0].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].periodeListe[0].grunnlagReferanseListe[1],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].periodeListe[1].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe!![1].periodeListe[1].grunnlagReferanseListe[1],
                )
            },

            // Engangsbeløp
            Executable { assertThat(vedtakFunnet.engangsbeløpListe.size).isEqualTo(3) },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[0].type).isEqualTo(nyttVedtakRequest.engangsbeløpListe!![0].type) },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[0].sak).isEqualTo(nyttVedtakRequest.engangsbeløpListe!![0].sak) },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[0].skyldner.toString()).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe!![0].skyldner.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[0].kravhaver.toString()).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe!![0].kravhaver.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[0].mottaker.toString()).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe!![0].mottaker.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[0].beløp?.toInt()).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe!![0].beløp?.toInt(),
                )
            },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[0].valutakode).isEqualTo(nyttVedtakRequest.engangsbeløpListe!![0].valutakode) },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[0].resultatkode)
                    .isEqualTo(nyttVedtakRequest.engangsbeløpListe!![0].resultatkode)
            },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[0].innkreving).isEqualTo(nyttVedtakRequest.engangsbeløpListe!![0].innkreving) },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[0].beslutning).isEqualTo(nyttVedtakRequest.engangsbeløpListe!![0].beslutning) },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[0].omgjørVedtakId).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe!![0].omgjørVedtakId,
                )
            },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[0].referanse).isEqualTo(nyttVedtakRequest.engangsbeløpListe!![0].referanse) },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[0].delytelseId).isEqualTo(nyttVedtakRequest.engangsbeløpListe!![0].delytelseId) },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[0].eksternReferanse).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe!![0].eksternReferanse,
                )
            },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[0].grunnlagReferanseListe.size).isEqualTo(3) },

            Executable { assertThat(vedtakFunnet.engangsbeløpListe[1].type).isEqualTo(nyttVedtakRequest.engangsbeløpListe!![1].type) },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[1].sak).isEqualTo(nyttVedtakRequest.engangsbeløpListe!![1].sak) },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[1].skyldner.toString()).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe!![1].skyldner.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[1].kravhaver.toString()).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe!![1].kravhaver.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[1].mottaker.toString()).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe!![1].mottaker.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[1].beløp?.toInt()).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe!![1].beløp?.toInt(),
                )
            },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[1].valutakode).isEqualTo(nyttVedtakRequest.engangsbeløpListe!![1].valutakode) },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[1].resultatkode)
                    .isEqualTo(nyttVedtakRequest.engangsbeløpListe!![1].resultatkode)
            },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[1].innkreving).isEqualTo(nyttVedtakRequest.engangsbeløpListe!![1].innkreving) },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[1].beslutning).isEqualTo(nyttVedtakRequest.engangsbeløpListe!![1].beslutning) },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[1].omgjørVedtakId).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe!![1].omgjørVedtakId,
                )
            },
            // Tester på at det genereres en referanse hvis den ikke er angitt i requesten
            Executable { assertThat(nyttVedtakRequest.engangsbeløpListe!![1].referanse).isNull() },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[1].referanse).isNotNull() },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[1].delytelseId).isEqualTo(nyttVedtakRequest.engangsbeløpListe!![1].delytelseId) },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[1].eksternReferanse).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe!![1].eksternReferanse,
                )
            },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[1].grunnlagReferanseListe.size).isEqualTo(3) },

            // Behandlingsreferanse
            Executable { assertThat(vedtakFunnet.behandlingsreferanseListe.size).isEqualTo(2) },
            Executable {
                assertThat(vedtakFunnet.behandlingsreferanseListe[0].kilde)
                    .isEqualTo(nyttVedtakRequest.behandlingsreferanseListe!![0].kilde)
            },
            Executable {
                assertThat(vedtakFunnet.behandlingsreferanseListe[0].referanse).isEqualTo(
                    nyttVedtakRequest.behandlingsreferanseListe!![0].referanse,
                )
            },

        )
    }

    @Test
    fun `test på at HttpClientErrorException kastes ved to like referanser for engangsbeløp i OpprettVedtakRequestDto`() {
        // Oppretter nytt vedtak
        val nyttVedtakRequest = byggVedtakMedDuplikateReferanserRequest()

        assertThatExceptionOfType(HttpClientErrorException::class.java).isThrownBy {
            vedtakService.opprettVedtak(nyttVedtakRequest)
        }
    }

    @Test
    fun `duplikattest engangsbeløpreferanse, skal returnere true`() {
        // Opprett mock av OpprettEngangsbeløpRequestDto
        val engangsbeløpMock1 = mock<OpprettEngangsbeløpRequestDto>()
        val engangsbeløpMock2 = mock<OpprettEngangsbeløpRequestDto>()
        val engangsbeløpMock3 = mock<OpprettEngangsbeløpRequestDto>()

        // Angi referansene for mock-objektene
        `when`(engangsbeløpMock1.referanse).thenReturn("referanse1")
        `when`(engangsbeløpMock2.referanse).thenReturn("referanse1")
        `when`(engangsbeløpMock3.referanse).thenReturn("referanse2")

        // Kjør testen
        val resultat = vedtakService.duplikateReferanser(listOf(engangsbeløpMock1, engangsbeløpMock2, engangsbeløpMock3))

        // Utfør nødvendige assertjekker basert på forventet resultat
        assert(resultat) { "Forventet true, men fikk false" }
    }

    @Test
    fun `test duplikateReferanser skal returnere false når det ikke finnes duplikater i engangsbeløpreferanse`() {
        // Opprett mock av OpprettEngangsbeløpRequestDto
        val engangsbeløpMock1 = mock<OpprettEngangsbeløpRequestDto>()
        val engangsbeløpMock2 = mock<OpprettEngangsbeløpRequestDto>()

        // Angi ulike referanser for mock-objektene
        `when`(engangsbeløpMock1.referanse).thenReturn("referanse1")
        `when`(engangsbeløpMock2.referanse).thenReturn("referanse2")

        // Kjør testen
        val resultat = vedtakService.duplikateReferanser(listOf(engangsbeløpMock1, engangsbeløpMock2))

        // Utfør nødvendige assertjekker basert på forventet resultat
        assert(!resultat) { "Forventet false, men fikk true" }
    }

    @Test
    @Disabled
    @Suppress("NonAsciiCharacters")
    fun `skal opprette vedtak uten grunnlag og så oppdatere vedtak med grunnlag`() {
        // Oppretter nytt vedtak
        val oopdaterVedtakUtenGrunnlagRequest = byggVedtakRequestUtenGrunnlag()
        val vedtakUtenGrunnlagVedtakId = vedtakService.opprettVedtak(oopdaterVedtakUtenGrunnlagRequest).vedtaksid

        // Henter vedtak uten grunnlag
        val vedtakUtenGrunnlag = vedtakService.hentVedtak(vedtakUtenGrunnlagVedtakId)

        val oopdaterVedtakMedGrunnlagRequest = byggVedtakRequest()

        vedtakService.oppdaterVedtak(vedtakUtenGrunnlagVedtakId, oopdaterVedtakMedGrunnlagRequest)

        // Henter oppdatert vedtak
        val oppdatertVedtakMedGrunnlag = vedtakService.hentVedtak(vedtakUtenGrunnlagVedtakId)

        assertAll(

            // Grunnlag
            Executable { assertThat(vedtakUtenGrunnlag.grunnlagListe).isEmpty() },

            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[0].referanse).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.grunnlagListe[0].referanse,
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[0].type).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.grunnlagListe[0].type,
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[0].innhold).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.grunnlagListe[0].innhold,
                )
            },

            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[1].referanse).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.grunnlagListe[1].referanse,
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[1].type).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.grunnlagListe[1].type,
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[1].innhold).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.grunnlagListe[1].innhold,
                )
            },

            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[2].referanse).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.grunnlagListe[2].referanse,
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[2].type).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.grunnlagListe[2].type,
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[2].innhold).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.grunnlagListe[2].innhold,
                )
            },

            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[3].referanse).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.grunnlagListe[3].referanse,
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[3].type).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.grunnlagListe[3].type,
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[3].innhold).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.grunnlagListe[3].innhold,
                )
            },

            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[4].referanse).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.grunnlagListe[4].referanse,
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[4].type).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.grunnlagListe[4].type,
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[4].innhold).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.grunnlagListe[4].innhold,
                )
            },

            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[5].referanse).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.grunnlagListe[5].referanse,
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[5].type).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.grunnlagListe[5].type,
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe[5].innhold).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.grunnlagListe[5].innhold,
                )
            },

            Executable { assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe.size).isEqualTo(8) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe.size).isEqualTo(2) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.behandlingsreferanseListe.size).isEqualTo(2) },

            // Periode
            Executable { assertThat(vedtakUtenGrunnlag.stønadsendringListe[0].grunnlagReferanseListe).isEmpty() },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].grunnlagReferanseListe.size).isEqualTo(2) },

            Executable { assertThat(vedtakUtenGrunnlag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe).isEmpty() },
            Executable { assertThat(vedtakUtenGrunnlag.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe).isEmpty() },
            Executable { assertThat(vedtakUtenGrunnlag.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe).isEmpty() },
            Executable { assertThat(vedtakUtenGrunnlag.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe).isEmpty() },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(3) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(4) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(2) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(2) },

            // StønadsendringGrunnlag
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe!![0].grunnlagReferanseListe[0],
                )
            },

            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe!![0].grunnlagReferanseListe[1],
                )
            },

            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe!![1].grunnlagReferanseListe[0],
                )
            },

            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe!![1].grunnlagReferanseListe[1],
                )
            },

            // GrunnlagReferanse
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[1],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[2]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[2],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[1],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[2]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[2],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[3]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[3],
                )
            },

            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe!![1].periodeListe[0].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe!![1].periodeListe[0].grunnlagReferanseListe[1],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe!![1].periodeListe[1].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe!![1].periodeListe[1].grunnlagReferanseListe[1],
                )
            },

            // Engangsbeløp
            Executable { assertThat(oppdatertVedtakMedGrunnlag.engangsbeløpListe.size).isEqualTo(2) },

            Executable { assertThat(vedtakUtenGrunnlag.engangsbeløpListe[0].grunnlagReferanseListe).isEmpty() },
            Executable { assertThat(vedtakUtenGrunnlag.engangsbeløpListe[1].grunnlagReferanseListe).isEmpty() },

            Executable { assertThat(oppdatertVedtakMedGrunnlag.engangsbeløpListe[0].grunnlagReferanseListe.size).isEqualTo(3) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.engangsbeløpListe[1].grunnlagReferanseListe.size).isEqualTo(3) },

        )
    }

    @Disabled
    @Test
    @Suppress("NonAsciiCharacters")
    fun `sjekk på at eventuelt eksisterende grunnlag på vedtak slettes før oppdatering av vedtak`() {
        // Oppretter nytt vedtak
        val vedtakRequest = byggVedtakRequest()
        val vedtakId = vedtakService.opprettVedtak(vedtakRequest).vedtaksid

        // Henter vedtak uten grunnlag
        val vedtak = vedtakService.hentVedtak(vedtakId)

        val oppdaterVedtakMedGrunnlagRequest = byggVedtakRequest()

        vedtakService.oppdaterVedtak(vedtakId, oppdaterVedtakMedGrunnlagRequest)

        // Henter oppdatert vedtak
        val oppdatertVedtakMedGrunnlag = vedtakService.hentVedtak(vedtakId)

        assertAll(

            // Grunnlag
            Executable { assertThat(vedtak.grunnlagListe.size).isEqualTo(8) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.grunnlagListe.size).isEqualTo(8) },

            // Periode
            Executable { assertThat(vedtak.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(3) },
            Executable { assertThat(vedtak.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(4) },
            Executable { assertThat(vedtak.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(2) },
            Executable { assertThat(vedtak.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(2) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(3) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(4) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(2) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(2) },

            // GrunnlagReferanse
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[1],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[2]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe!![0].periodeListe[0].grunnlagReferanseListe[2],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[1],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[2]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[2],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[3]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe!![0].periodeListe[1].grunnlagReferanseListe[3],
                )
            },

            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe!![1].periodeListe[0].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe!![1].periodeListe[0].grunnlagReferanseListe[1],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe!![1].periodeListe[1].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe!![1].periodeListe[1].grunnlagReferanseListe[1],
                )
            },

            // Engangsbeløp
            Executable { assertThat(oppdatertVedtakMedGrunnlag.engangsbeløpListe.size).isEqualTo(2) },

            Executable { assertThat(vedtak.engangsbeløpListe[0].grunnlagReferanseListe.size).isEqualTo(3) },
            Executable { assertThat(vedtak.engangsbeløpListe[1].grunnlagReferanseListe.size).isEqualTo(3) },

            Executable { assertThat(oppdatertVedtakMedGrunnlag.engangsbeløpListe[0].grunnlagReferanseListe.size).isEqualTo(3) },
            Executable { assertThat(oppdatertVedtakMedGrunnlag.engangsbeløpListe[1].grunnlagReferanseListe.size).isEqualTo(3) },

        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `test at oppdatering av vedtak med mismatch på vedtak feiler`() {
        // Oppretter nytt vedtak
        val vedtak = byggVedtakRequest()
        val vedtakId = vedtakService.opprettVedtak(vedtak).vedtaksid

        val oppdaterVedtakMedGrunnlagRequest = byggOppdaterVedtakMedMismatchVedtak()

        assertThatExceptionOfType(VedtaksdataMatcherIkkeException::class.java).isThrownBy {
            vedtakService.oppdaterVedtak(vedtakId, oppdaterVedtakMedGrunnlagRequest)
        }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `test at oppdatering av vedtak med mismatch på stønadsendring feiler`() {
        // Oppretter nytt vedtak
        val vedtak = byggVedtakRequest()
        val vedtakId = vedtakService.opprettVedtak(vedtak).vedtaksid

        val oppdaterVedtakMedGrunnlagRequest = byggOppdaterVedtakMedMismatchStønadsendring()

        assertThatExceptionOfType(VedtaksdataMatcherIkkeException::class.java).isThrownBy {
            vedtakService.oppdaterVedtak(vedtakId, oppdaterVedtakMedGrunnlagRequest)
        }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `test at oppdatering av vedtak med mismatch på periode feiler`() {
        // Oppretter nytt vedtak
        val vedtak = byggVedtakRequest()
        val vedtakId = vedtakService.opprettVedtak(vedtak).vedtaksid

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
        val vedtakId = vedtakService.opprettVedtak(vedtak).vedtaksid

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
        val vedtakId = vedtakService.opprettVedtak(vedtak).vedtaksid

        val oppdaterVedtakMedGrunnlagRequest = byggVedtakRequestUtenGrunnlag()

        assertThatExceptionOfType(GrunnlagsdataManglerException::class.java).isThrownBy {
            vedtakService.oppdaterVedtak(vedtakId, oppdaterVedtakMedGrunnlagRequest)
        }
    }
}
