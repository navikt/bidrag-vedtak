package no.nav.bidrag.vedtak.service

import io.mockk.every
import io.mockk.mockkObject
import no.nav.bidrag.commons.service.organisasjon.SaksbehandlernavnProvider
import no.nav.bidrag.domene.enums.vedtak.Beslutningstype
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.transport.behandling.vedtak.request.HentVedtakForStønadRequest
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettEngangsbeløpRequestDto
import no.nav.bidrag.vedtak.BidragVedtakTest
import no.nav.bidrag.vedtak.TestUtil.Companion.byggOppdaterVedtakMedMismatchEngangsbeløp
import no.nav.bidrag.vedtak.TestUtil.Companion.byggOppdaterVedtakMedMismatchPeriode
import no.nav.bidrag.vedtak.TestUtil.Companion.byggOppdaterVedtakMedMismatchStønadsendring
import no.nav.bidrag.vedtak.TestUtil.Companion.byggOppdaterVedtakMedMismatchVedtak
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtakEngangsbeløpUtenReferanseRequest
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtakMedDuplikateReferanserRequest
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtakRequest
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtakRequestMedInputparametre
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtakRequestUtenGrunnlag
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtaksforslagMedOppdatertInnholdRequest
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtaksforslagRequest
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
import java.time.LocalDate
import java.time.YearMonth

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
        mockkObject(SaksbehandlernavnProvider)
        every {
            SaksbehandlernavnProvider.hentSaksbehandlernavn(any())
        } returns "Saksbehandler Saksbehandlersen"
    }

    @Test
    fun `skal opprette og hente vedtak`() {
        // Oppretter nytt vedtak
        val nyttVedtakRequest = byggVedtakRequest()
        val nyttVedtakOpprettet = vedtakService.opprettVedtak(nyttVedtakRequest, false).vedtaksid

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
            Executable { assertThat(vedtakFunnet.vedtakstidspunkt).isNotNull() },
            Executable { assertThat(vedtakFunnet.opprettetAv).isEqualTo(nyttVedtakRequest.opprettetAv) },
            Executable { assertThat(vedtakFunnet.unikReferanse).isEqualTo(nyttVedtakRequest.unikReferanse) },
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
            Executable { assertThat(vedtakFunnet.grunnlagListe[0].gjelderReferanse).isEqualTo("PERSON_BM") },
            Executable { assertThat(vedtakFunnet.grunnlagListe[0].grunnlagsreferanseListe).contains("innhentet_ainntekt_1", "innhentet_ainntekt_2") },

            Executable { assertThat(vedtakFunnet.grunnlagListe[1].referanse).isEqualTo(nyttVedtakRequest.grunnlagListe[1].referanse) },
            Executable { assertThat(vedtakFunnet.grunnlagListe[1].type).isEqualTo(nyttVedtakRequest.grunnlagListe[1].type) },
            Executable { assertThat(vedtakFunnet.grunnlagListe[1].innhold).isEqualTo(nyttVedtakRequest.grunnlagListe[1].innhold) },
            Executable { assertThat(vedtakFunnet.grunnlagListe[1].gjelderReferanse).isEqualTo("PERSON_BM") },
            Executable { assertThat(vedtakFunnet.grunnlagListe[1].grunnlagsreferanseListe).contains("innhentet_ainntekt_4", "innhentet_ainntekt_3") },

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
            Executable { assertThat(vedtakFunnet.stønadsendringListe[0].type).isEqualTo(nyttVedtakRequest.stønadsendringListe[0].type) },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[0].sak).isEqualTo(nyttVedtakRequest.stønadsendringListe[0].sak) },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].skyldner.toString()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].skyldner.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].kravhaver.toString()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].kravhaver.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].mottaker.toString()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].mottaker.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].førsteIndeksreguleringsår).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].førsteIndeksreguleringsår,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].innkreving)
                    .isEqualTo(nyttVedtakRequest.stønadsendringListe[0].innkreving)
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].beslutning)
                    .isEqualTo(nyttVedtakRequest.stønadsendringListe[0].beslutning)
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].omgjørVedtakId).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].omgjørVedtakId,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].eksternReferanse).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].eksternReferanse,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].grunnlagReferanseListe[1],
                )
            },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[0].grunnlagReferanseListe.size).isEqualTo(2) },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe.size).isEqualTo(2) },

            Executable { assertThat(vedtakFunnet.stønadsendringListe[1].type).isEqualTo(nyttVedtakRequest.stønadsendringListe[1].type) },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[1].sak).isEqualTo(nyttVedtakRequest.stønadsendringListe[1].sak) },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].skyldner.toString()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].skyldner.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].kravhaver.toString()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].kravhaver.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].mottaker.toString()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].mottaker.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].førsteIndeksreguleringsår).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].førsteIndeksreguleringsår,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].innkreving)
                    .isEqualTo(nyttVedtakRequest.stønadsendringListe[1].innkreving)
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].beslutning)
                    .isEqualTo(nyttVedtakRequest.stønadsendringListe[1].beslutning)
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].omgjørVedtakId).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].omgjørVedtakId,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].eksternReferanse).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].eksternReferanse,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].grunnlagReferanseListe[1],
                )
            },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe.size).isEqualTo(2) },

            // Periode
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].periode.fom).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].periodeListe[0].periode.fom,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].periode.til).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].periodeListe[0].periode.til,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].beløp?.toInt()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].periodeListe[0].beløp?.toInt(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].valutakode).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].periodeListe[0].valutakode,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].resultatkode).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].periodeListe[0].resultatkode,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].delytelseId).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].periodeListe[0].delytelseId,
                )
            },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(3) },

            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].periode.fom).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].periodeListe[1].periode.fom,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].periode.til).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].periodeListe[1].periode.til,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].beløp?.toInt()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].periodeListe[1].beløp?.toInt(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].valutakode).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].periodeListe[1].valutakode,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].resultatkode).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].periodeListe[1].resultatkode,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].delytelseId).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].periodeListe[1].delytelseId,
                )
            },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(4) },

            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[0].periode.fom).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].periodeListe[0].periode.fom,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[0].periode.til).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].periodeListe[0].periode.til,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[0].beløp?.toInt()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].periodeListe[0].beløp?.toInt(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[0].valutakode).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].periodeListe[0].valutakode,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[0].resultatkode).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].periodeListe[0].resultatkode,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[0].delytelseId).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].periodeListe[0].delytelseId,
                )
            },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(2) },

            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[1].periode.fom).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].periodeListe[1].periode.fom,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[1].periode.til).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].periodeListe[1].periode.til,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[1].beløp?.toInt()).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].periodeListe[1].beløp?.toInt(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[1].valutakode).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].periodeListe[1].valutakode,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[1].resultatkode).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].periodeListe[1].resultatkode,
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[1].delytelseId).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].periodeListe[1].delytelseId,
                )
            },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe.size).isEqualTo(2) },

            // GrunnlagReferanse
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[1],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[2]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[2],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[1],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[2]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[2],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[3]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[3],
                )
            },

            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe[1],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(vedtakFunnet.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    nyttVedtakRequest.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe[1],
                )
            },

            // Engangsbeløp
            Executable { assertThat(vedtakFunnet.engangsbeløpListe.size).isEqualTo(3) },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[0].type).isEqualTo(nyttVedtakRequest.engangsbeløpListe[0].type) },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[0].sak).isEqualTo(nyttVedtakRequest.engangsbeløpListe[0].sak) },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[0].skyldner.toString()).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe[0].skyldner.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[0].kravhaver.toString()).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe[0].kravhaver.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[0].mottaker.toString()).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe[0].mottaker.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[0].beløp?.toInt()).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe[0].beløp?.toInt(),
                )
            },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[0].valutakode).isEqualTo(nyttVedtakRequest.engangsbeløpListe[0].valutakode) },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[0].resultatkode)
                    .isEqualTo(nyttVedtakRequest.engangsbeløpListe[0].resultatkode)
            },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[0].innkreving).isEqualTo(nyttVedtakRequest.engangsbeløpListe[0].innkreving) },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[0].beslutning).isEqualTo(nyttVedtakRequest.engangsbeløpListe[0].beslutning) },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[0].omgjørVedtakId).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe[0].omgjørVedtakId,
                )
            },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[0].referanse).isEqualTo(nyttVedtakRequest.engangsbeløpListe[0].referanse) },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[0].delytelseId).isEqualTo(nyttVedtakRequest.engangsbeløpListe[0].delytelseId) },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[0].eksternReferanse).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe[0].eksternReferanse,
                )
            },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[0].grunnlagReferanseListe.size).isEqualTo(3) },

            Executable { assertThat(vedtakFunnet.engangsbeløpListe[1].type).isEqualTo(nyttVedtakRequest.engangsbeløpListe[1].type) },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[1].sak).isEqualTo(nyttVedtakRequest.engangsbeløpListe[1].sak) },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[1].skyldner.toString()).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe[1].skyldner.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[1].kravhaver.toString()).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe[1].kravhaver.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[1].mottaker.toString()).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe[1].mottaker.toString(),
                )
            },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[1].beløp?.toInt()).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe[1].beløp?.toInt(),
                )
            },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[1].valutakode).isEqualTo(nyttVedtakRequest.engangsbeløpListe[1].valutakode) },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[1].resultatkode)
                    .isEqualTo(nyttVedtakRequest.engangsbeløpListe[1].resultatkode)
            },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[1].innkreving).isEqualTo(nyttVedtakRequest.engangsbeløpListe[1].innkreving) },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[1].beslutning).isEqualTo(nyttVedtakRequest.engangsbeløpListe[1].beslutning) },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[1].omgjørVedtakId).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe[1].omgjørVedtakId,
                )
            },
            // Tester på at det genereres en referanse hvis den ikke er angitt i requesten
            Executable { assertThat(nyttVedtakRequest.engangsbeløpListe[1].referanse).isNull() },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[1].referanse).isNotNull() },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[1].delytelseId).isEqualTo(nyttVedtakRequest.engangsbeløpListe[1].delytelseId) },
            Executable {
                assertThat(vedtakFunnet.engangsbeløpListe[1].eksternReferanse).isEqualTo(
                    nyttVedtakRequest.engangsbeløpListe[1].eksternReferanse,
                )
            },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[1].grunnlagReferanseListe.size).isEqualTo(3) },

            // Behandlingsreferanse
            Executable { assertThat(vedtakFunnet.behandlingsreferanseListe.size).isEqualTo(2) },
            Executable {
                assertThat(vedtakFunnet.behandlingsreferanseListe[0].kilde)
                    .isEqualTo(nyttVedtakRequest.behandlingsreferanseListe[0].kilde)
            },
            Executable {
                assertThat(vedtakFunnet.behandlingsreferanseListe[0].referanse).isEqualTo(
                    nyttVedtakRequest.behandlingsreferanseListe[0].referanse,
                )
            },

        )
    }

    @Test
    fun `test på at HttpClientErrorException kastes ved to like referanser for engangsbeløp i OpprettVedtakRequestDto`() {
        // Oppretter nytt vedtak
        val nyttVedtakRequest = byggVedtakMedDuplikateReferanserRequest()

        assertThatExceptionOfType(HttpClientErrorException::class.java).isThrownBy {
            vedtakService.opprettVedtak(nyttVedtakRequest, false)
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
        val oppdaterVedtakUtenGrunnlagRequest = byggVedtakRequestUtenGrunnlag()
        val vedtakUtenGrunnlagVedtakId = vedtakService.opprettVedtak(oppdaterVedtakUtenGrunnlagRequest, false).vedtaksid

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
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe[0].grunnlagReferanseListe[0],
                )
            },

            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe[0].grunnlagReferanseListe[1],
                )
            },

            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe[1].grunnlagReferanseListe[0],
                )
            },

            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe[1].grunnlagReferanseListe[1],
                )
            },

            // GrunnlagReferanse
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[1],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[2]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[2],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[1],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[2]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[2],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[3]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[3],
                )
            },

            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe[1],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    oopdaterVedtakMedGrunnlagRequest.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe[1],
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
        val vedtakId = vedtakService.opprettVedtak(vedtakRequest, false).vedtaksid

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
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[1],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[2]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[2],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[1],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[2]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[2],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[3]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe[0].periodeListe[1].grunnlagReferanseListe[3],
                )
            },

            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe[1]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe[1].periodeListe[0].grunnlagReferanseListe[1],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe[0]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe[0],
                )
            },
            Executable {
                assertThat(oppdatertVedtakMedGrunnlag.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe[1]).isEqualTo(
                    oppdaterVedtakMedGrunnlagRequest.stønadsendringListe[1].periodeListe[1].grunnlagReferanseListe[1],
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
        val vedtakId = vedtakService.opprettVedtak(vedtak, false).vedtaksid

        val oppdaterVedtakMedGrunnlagRequest = byggOppdaterVedtakMedMismatchVedtak()

        assertThatExceptionOfType(VedtaksdataMatcherIkkeException::class.java).isThrownBy {
            vedtakService.oppdaterVedtak(vedtakId, oppdaterVedtakMedGrunnlagRequest)
        }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `test at oppdatering av vedtak med mismatch på stønadsendring feiler`() {
        // Oppretter nytt vedtak
        val request = byggVedtakRequest()
        val vedtakId = vedtakService.opprettVedtak(request, false).vedtaksid

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
        val vedtakId = vedtakService.opprettVedtak(vedtak, false).vedtaksid

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
        val vedtakId = vedtakService.opprettVedtak(vedtak, false).vedtaksid

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
        val vedtakId = vedtakService.opprettVedtak(vedtak, false).vedtaksid

        val oppdaterVedtakMedGrunnlagRequest = byggVedtakRequestUtenGrunnlag()

        assertThatExceptionOfType(GrunnlagsdataManglerException::class.java).isThrownBy {
            vedtakService.oppdaterVedtak(vedtakId, oppdaterVedtakMedGrunnlagRequest)
        }
    }

    @Test
    fun `test hent av vedtak for stønad`() {
        // Oppretter nytt vedtak
        val nyttVedtakRequest = byggVedtakRequestMedInputparametre(
            vedtaksdato = LocalDate.now(),
            vedtakstype = Vedtakstype.ENDRING,
            saksnummer = Saksnummer("SAK-001"),
            type = Stønadstype.BIDRAG,
            skyldner = Personident("11111111111"),
            kravhaver = Personident("2222222222"),
            innkreving = Innkrevingstype.MED_INNKREVING,
            beslutning = Beslutningstype.ENDRING,
        )
        val nyttVedtakOpprettet = vedtakService.opprettVedtak(nyttVedtakRequest, false).vedtaksid

        assertAll(
            Executable { assertThat(nyttVedtakOpprettet).isNotNull() },
        )

        val request = HentVedtakForStønadRequest(
            sak = Saksnummer("SAK-001"),
            type = Stønadstype.BIDRAG,
            skyldner = Personident("11111111111"),
            kravhaver = Personident("2222222222"),
        )

        // Henter vedtak
        val vedtakFunnet = vedtakService.hentVedtakForStønad(request).vedtakListe.first()

        assertAll(
            Executable { assertThat(vedtakFunnet).isNotNull() },

            // Vedtak
            Executable { assertThat(vedtakFunnet.vedtaksid).isEqualTo(nyttVedtakOpprettet.toLong()) },
            Executable { assertThat(vedtakFunnet.vedtakstidspunkt).isEqualTo(nyttVedtakRequest.vedtakstidspunkt) },
            Executable { assertThat(vedtakFunnet.type).isEqualTo(nyttVedtakRequest.type) },

            Executable { assertThat(vedtakFunnet.stønadsendring.sak).isEqualTo(Saksnummer("SAK-001")) },
            Executable { assertThat(vedtakFunnet.stønadsendring.type).isEqualTo(Stønadstype.BIDRAG) },
            Executable { assertThat(vedtakFunnet.stønadsendring.skyldner).isEqualTo(Personident("11111111111")) },
            Executable { assertThat(vedtakFunnet.stønadsendring.kravhaver).isEqualTo(Personident("2222222222")) },
            Executable { assertThat(vedtakFunnet.stønadsendring.innkreving).isEqualTo(Innkrevingstype.MED_INNKREVING) },
            Executable { assertThat(vedtakFunnet.stønadsendring.beslutning).isEqualTo(Beslutningstype.ENDRING) },
            Executable { assertThat(vedtakFunnet.stønadsendring.omgjørVedtakId).isNull() },
            Executable { assertThat(vedtakFunnet.stønadsendring.periodeListe.size).isEqualTo(2) },

            Executable { assertThat(vedtakFunnet.stønadsendring.periodeListe[0].periode.fom).isEqualTo(YearMonth.parse("2019-01")) },
            Executable { assertThat(vedtakFunnet.stønadsendring.periodeListe[0].periode.til).isEqualTo(YearMonth.parse("2019-07")) },
            Executable { assertThat(vedtakFunnet.stønadsendring.periodeListe[0].beløp!!.toInt()).isEqualTo(3490) },
            Executable { assertThat(vedtakFunnet.stønadsendring.periodeListe[0].valutakode).isEqualTo("NOK") },
            Executable { assertThat(vedtakFunnet.stønadsendring.periodeListe[0].resultatkode).isEqualTo("KOSTNADSBEREGNET_BIDRAG") },
            Executable { assertThat(vedtakFunnet.stønadsendring.periodeListe[0].grunnlagReferanseListe).isEmpty() },

            Executable { assertThat(vedtakFunnet.stønadsendring.periodeListe[1].periode.fom).isEqualTo(YearMonth.parse("2019-07")) },
            Executable { assertThat(vedtakFunnet.stønadsendring.periodeListe[1].periode.til).isEqualTo(YearMonth.parse("2020-01")) },
            Executable { assertThat(vedtakFunnet.stønadsendring.periodeListe[1].beløp!!.toInt()).isEqualTo(3520) },
            Executable { assertThat(vedtakFunnet.stønadsendring.periodeListe[1].valutakode).isEqualTo("NOK") },
            Executable { assertThat(vedtakFunnet.stønadsendring.periodeListe[1].resultatkode).isEqualTo("KOSTNADSBEREGNET_BIDRAG") },
            Executable { assertThat(vedtakFunnet.stønadsendring.periodeListe[1].grunnlagReferanseListe).isEmpty() },

        )
    }

    @Test
    fun `test hent av flere vedtak for stønad med og uten innkreving`() {
        // Oppretter nytt vedtak
        val vedtakRequest1 = byggVedtakRequestMedInputparametre(
            LocalDate.now().minusMonths(2),
            null,
            null,
            null,
            null,
            null,
            Innkrevingstype.MED_INNKREVING,
            null,
        )
        val vedtakOpprettet1 = vedtakService.opprettVedtak(vedtakRequest1, false).vedtaksid

        assertAll(
            Executable { assertThat(vedtakOpprettet1).isNotNull() },
        )

        val vedtakRequest2 = byggVedtakRequestMedInputparametre(
            LocalDate.now().minusMonths(1),
            null,
            null,
            null,
            null,
            null,
            Innkrevingstype.UTEN_INNKREVING,
            null,
        )
        val vedtakOpprettet2 = vedtakService.opprettVedtak(vedtakRequest2, false).vedtaksid

        assertAll(
            Executable { assertThat(vedtakOpprettet2).isNotNull() },
        )

        val vedtakRequest3 = byggVedtakRequestMedInputparametre(
            LocalDate.now(),
            null,
            null,
            null,
            null,
            null,
            Innkrevingstype.MED_INNKREVING,
            null,
        )
        val vedtakOpprettet3 = vedtakService.opprettVedtak(vedtakRequest3, false).vedtaksid

        assertAll(
            Executable { assertThat(vedtakOpprettet3).isNotNull() },
        )

        val request = HentVedtakForStønadRequest(
            sak = Saksnummer("SAK-001"),
            type = Stønadstype.BIDRAG,
            skyldner = Personident("1"),
            kravhaver = Personident("2"),
        )

        // Henter vedtak
        val vedtakFunnet = vedtakService.hentVedtakForStønad(request).vedtakListe
        val vedtak1 = vedtakFunnet[0]
        val vedtak2 = vedtakFunnet[1]
        val vedtak3 = vedtakFunnet[2]

        assertAll(
            Executable { assertThat(vedtakFunnet.size).isEqualTo(3) },

            // Vedtak
            Executable { assertThat(vedtak1.vedtaksid).isEqualTo(vedtakOpprettet1.toLong()) },
            Executable { assertThat(vedtak1.vedtakstidspunkt.toLocalDate()).isEqualTo(LocalDate.now().minusMonths(2)) },

            Executable { assertThat(vedtak2.vedtaksid).isEqualTo(vedtakOpprettet2.toLong()) },
            Executable { assertThat(vedtak2.vedtakstidspunkt.toLocalDate()).isEqualTo(LocalDate.now().minusMonths(1)) },

            Executable { assertThat(vedtak3.vedtaksid).isEqualTo(vedtakOpprettet3.toLong()) },
            Executable { assertThat(vedtak3.vedtakstidspunkt.toLocalDate()).isEqualTo(LocalDate.now()) },

        )
    }

    @Test
    fun `test at kun bidragssaker hentes for stønad`() {
        // Oppretter nytt vedtak
        val vedtakRequest1 = byggVedtakRequestMedInputparametre(
            LocalDate.now().minusMonths(2),
            null,
            null,
            Stønadstype.BIDRAG18AAR,
            null,
            null,
            null,
            null,
        )
        val vedtakOpprettet1 = vedtakService.opprettVedtak(vedtakRequest1, false).vedtaksid

        assertAll(
            Executable { assertThat(vedtakOpprettet1).isNotNull() },
        )

        // Vedtak uten innkreving, skal ikke komme i responsen under
        val vedtakRequest2 = byggVedtakRequestMedInputparametre(
            LocalDate.now().minusMonths(1),
            null,
            null,
            Stønadstype.FORSKUDD,
            null,
            null,
            null,
            null,
        )
        val vedtakOpprettet2 = vedtakService.opprettVedtak(vedtakRequest2, false).vedtaksid

        assertAll(
            Executable { assertThat(vedtakOpprettet2).isNotNull() },
        )

        val request = HentVedtakForStønadRequest(
            sak = Saksnummer("SAK-001"),
            type = Stønadstype.BIDRAG18AAR,
            skyldner = Personident("1"),
            kravhaver = Personident("2"),
        )

        // Henter vedtak
        val vedtakFunnet = vedtakService.hentVedtakForStønad(request).vedtakListe
        val vedtak1 = vedtakFunnet[0]

        assertAll(
            Executable { assertThat(vedtakFunnet.size).isEqualTo(1) },

            // Vedtak
            Executable { assertThat(vedtak1.vedtaksid).isEqualTo(vedtakOpprettet1.toLong()) },
            Executable { assertThat(vedtak1.vedtakstidspunkt.toLocalDate()).isEqualTo(LocalDate.now().minusMonths(2)) },
            Executable { assertThat(vedtak1.stønadsendring.type).isEqualTo(Stønadstype.BIDRAG18AAR) },

        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `test at omgjøringsvedtak for engangsbeløp uten referanse feiler`() {
        // Oppretter nytt vedtak der det ikke er angitt referanse på engansbeløpet det er klaget på
        val request = byggVedtakEngangsbeløpUtenReferanseRequest()

        assertThatExceptionOfType(HttpClientErrorException::class.java).isThrownBy {
            vedtakService.opprettVedtak(request, false)
        }
    }

    @Test
    fun `skal opprette og hente vedtaksforslag`() {
        // Oppretter nytt vedtaksforslag
        val nyttVedtaksforslagRequest = byggVedtaksforslagRequest()
        val nyttVedtaksforslagOpprettetVedtaksid = vedtakService.opprettVedtak(
            vedtakRequest = nyttVedtaksforslagRequest,
            vedtaksforslag = true,
        ).vedtaksid

        assertAll(
            Executable { assertThat(nyttVedtaksforslagOpprettetVedtaksid).isNotNull() },
        )

        // Henter vedtaksforslag
        val vedtaksforslagFunnet = vedtakService.hentVedtak(nyttVedtaksforslagOpprettetVedtaksid)

        assertAll(
            // Vedtaksforslag
            Executable { assertThat(vedtaksforslagFunnet.vedtakstidspunkt).isNull() },
        )
    }

    @Test
    fun `skal oppdatere vedtaksforslag`() {
        // Oppretter nytt vedtaksforslag
        val nyttVedtaksforslagRequest = byggVedtaksforslagRequest()
        val nyttVedtaksforslagOpprettetVedtaksid = vedtakService.opprettVedtak(
            vedtakRequest = nyttVedtaksforslagRequest,
            vedtaksforslag = true,
        ).vedtaksid

        // Henter vedtaksforslag
        val førsteVedtaksforslag = vedtakService.hentVedtak(nyttVedtaksforslagOpprettetVedtaksid)

        // Oppdaterer vedtaksforslag
        val oppdaterVedtaksforslagRequest = byggVedtaksforslagMedOppdatertInnholdRequest()

        val oppdatertVedtaksforslagVedtaksid = vedtakService.oppdaterVedtaksforslag(
            vedtaksid = nyttVedtaksforslagOpprettetVedtaksid,
            vedtakRequest = oppdaterVedtaksforslagRequest,
        )

        // Henter oppdatert vedtaksforslag
        val oppdatertVedtaksforslag = vedtakService.hentVedtak(oppdatertVedtaksforslagVedtaksid)

        assertAll(
            // Vedtaksforslag

            Executable { assertThat(førsteVedtaksforslag).isNotNull() },
            Executable { assertThat(oppdatertVedtaksforslag).isNotNull() },

            Executable { assertThat(førsteVedtaksforslag.vedtakstidspunkt).isNull() },
            Executable { assertThat(oppdatertVedtaksforslag.vedtakstidspunkt).isNull() },

            Executable { assertThat(nyttVedtaksforslagOpprettetVedtaksid == oppdatertVedtaksforslagVedtaksid).isTrue() },

            // Vedtak
            Executable { assertThat(oppdatertVedtaksforslag.kilde).isEqualTo(oppdaterVedtaksforslagRequest.kilde) },
            Executable { assertThat(oppdatertVedtaksforslag.type).isEqualTo(oppdaterVedtaksforslagRequest.type) },
//      Det fjernes 3 desimaler fra vedtakstidspunkt etter lagring, Postgres-feature?
//      Executable { assertThat(oppdatertVedtaksforslag.vedtakstidspunkt).isEqualTo(nyttVedtakRequest.vedtakstidspunkt) },
            Executable { assertThat(oppdatertVedtaksforslag.opprettetTidspunkt).isNotNull() },
            Executable { assertThat(oppdatertVedtaksforslag.vedtakstidspunkt).isNull() },
            Executable { assertThat(oppdatertVedtaksforslag.opprettetAv).isEqualTo(oppdaterVedtaksforslagRequest.opprettetAv) },
            Executable { assertThat(oppdatertVedtaksforslag.unikReferanse).isEqualTo(oppdaterVedtaksforslagRequest.unikReferanse) },
            Executable { assertThat(oppdatertVedtaksforslag.enhetsnummer).isEqualTo(oppdaterVedtaksforslagRequest.enhetsnummer) },
            Executable { assertThat(oppdatertVedtaksforslag.innkrevingUtsattTilDato).isEqualTo(oppdaterVedtaksforslagRequest.innkrevingUtsattTilDato) },
            Executable { assertThat(oppdatertVedtaksforslag.fastsattILand).isEqualTo(oppdaterVedtaksforslagRequest.fastsattILand) },
            Executable { assertThat(oppdatertVedtaksforslag.grunnlagListe.size).isEqualTo(1) },
            Executable { assertThat(oppdatertVedtaksforslag.stønadsendringListe.size).isEqualTo(1) },
            Executable { assertThat(oppdatertVedtaksforslag.behandlingsreferanseListe.size).isEqualTo(1) },

            // Grunnlag
            Executable { assertThat(oppdatertVedtaksforslag.grunnlagListe[0].referanse).isEqualTo(oppdaterVedtaksforslagRequest.grunnlagListe[0].referanse) },
            Executable { assertThat(oppdatertVedtaksforslag.grunnlagListe[0].type).isEqualTo(oppdaterVedtaksforslagRequest.grunnlagListe[0].type) },
            Executable { assertThat(oppdatertVedtaksforslag.grunnlagListe[0].innhold).isEqualTo(oppdaterVedtaksforslagRequest.grunnlagListe[0].innhold) },
            Executable { assertThat(oppdatertVedtaksforslag.grunnlagListe[0].gjelderReferanse).isEqualTo("PERSON_BM") },
            Executable { assertThat(oppdatertVedtaksforslag.grunnlagListe[0].grunnlagsreferanseListe).isEmpty() },
            Executable { assertThat(oppdatertVedtaksforslag.grunnlagListe.size).isEqualTo(oppdaterVedtaksforslagRequest.grunnlagListe.size) },
            // Stønadsendring
            Executable { assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].type).isEqualTo(oppdaterVedtaksforslagRequest.stønadsendringListe[0].type) },
            Executable { assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].sak).isEqualTo(oppdaterVedtaksforslagRequest.stønadsendringListe[0].sak) },
            Executable {
                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].skyldner.toString()).isEqualTo(
                    oppdaterVedtaksforslagRequest.stønadsendringListe[0].skyldner.toString(),
                )
            },
            Executable {
                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].kravhaver.toString()).isEqualTo(
                    oppdaterVedtaksforslagRequest.stønadsendringListe[0].kravhaver.toString(),
                )
            },
            Executable {
                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].mottaker.toString()).isEqualTo(
                    oppdaterVedtaksforslagRequest.stønadsendringListe[0].mottaker.toString(),
                )
            },
            Executable {
                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].førsteIndeksreguleringsår).isEqualTo(
                    oppdaterVedtaksforslagRequest.stønadsendringListe[0].førsteIndeksreguleringsår,
                )
            },
            Executable {
                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].innkreving)
                    .isEqualTo(oppdaterVedtaksforslagRequest.stønadsendringListe[0].innkreving)
            },
            Executable {
                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].beslutning)
                    .isEqualTo(oppdaterVedtaksforslagRequest.stønadsendringListe[0].beslutning)
            },
            Executable {
                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].omgjørVedtakId).isEqualTo(
                    oppdaterVedtaksforslagRequest.stønadsendringListe[0].omgjørVedtakId,
                )
            },
            Executable {
                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].eksternReferanse).isEqualTo(
                    oppdaterVedtaksforslagRequest.stønadsendringListe[0].eksternReferanse,
                )
            },
//            Executable {
//                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].grunnlagReferanseListe[0]).isEqualTo(
//                    oppdaterVedtaksforslagRequest.stønadsendringListe[0].grunnlagReferanseListe[0],
//                )
//            },

            Executable {
                assertThat(oppdatertVedtaksforslag.stønadsendringListe.size)
                    .isEqualTo(oppdaterVedtaksforslagRequest.stønadsendringListe.size)
            },

            Executable { assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].grunnlagReferanseListe.size).isEqualTo(0) },

            Executable {
                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].grunnlagReferanseListe.size)
                    .isEqualTo(oppdaterVedtaksforslagRequest.stønadsendringListe[0].grunnlagReferanseListe.size)
            },

            Executable { assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].periodeListe.size).isEqualTo(1) },

            Executable {
                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].periodeListe.size)
                    .isEqualTo(oppdaterVedtaksforslagRequest.stønadsendringListe[0].periodeListe.size)
            },

            // Periode
            Executable {
                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].periodeListe[0].periode.fom).isEqualTo(
                    oppdaterVedtaksforslagRequest.stønadsendringListe[0].periodeListe[0].periode.fom,
                )
            },
            Executable {
                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].periodeListe[0].periode.til).isEqualTo(
                    oppdaterVedtaksforslagRequest.stønadsendringListe[0].periodeListe[0].periode.til,
                )
            },
            Executable {
                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].periodeListe[0].beløp?.toInt()).isEqualTo(
                    oppdaterVedtaksforslagRequest.stønadsendringListe[0].periodeListe[0].beløp?.toInt(),
                )
            },
            Executable {
                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].periodeListe[0].valutakode).isEqualTo(
                    oppdaterVedtaksforslagRequest.stønadsendringListe[0].periodeListe[0].valutakode,
                )
            },
            Executable {
                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].periodeListe[0].resultatkode).isEqualTo(
                    oppdaterVedtaksforslagRequest.stønadsendringListe[0].periodeListe[0].resultatkode,
                )
            },
            Executable {
                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].periodeListe[0].delytelseId).isEqualTo(
                    oppdaterVedtaksforslagRequest.stønadsendringListe[0].periodeListe[0].delytelseId,
                )
            },
            Executable { assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(1) },

            Executable {
                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size)
                    .isEqualTo(oppdaterVedtaksforslagRequest.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size)
            },

            // GrunnlagReferanse
            Executable {
                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    oppdaterVedtaksforslagRequest.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0],
                )
            },
            // Engangsbeløp
            Executable { assertThat(oppdatertVedtaksforslag.engangsbeløpListe.size).isEqualTo(1) },
            Executable {
                assertThat(oppdatertVedtaksforslag.engangsbeløpListe.size)
                    .isEqualTo(oppdaterVedtaksforslagRequest.engangsbeløpListe.size)
            },

            Executable { assertThat(oppdatertVedtaksforslag.engangsbeløpListe[0].type).isEqualTo(oppdaterVedtaksforslagRequest.engangsbeløpListe[0].type) },
            Executable { assertThat(oppdatertVedtaksforslag.engangsbeløpListe[0].sak).isEqualTo(oppdaterVedtaksforslagRequest.engangsbeløpListe[0].sak) },
            Executable {
                assertThat(oppdatertVedtaksforslag.engangsbeløpListe[0].skyldner.toString()).isEqualTo(
                    oppdaterVedtaksforslagRequest.engangsbeløpListe[0].skyldner.toString(),
                )
            },
            Executable {
                assertThat(oppdatertVedtaksforslag.engangsbeløpListe[0].kravhaver.toString()).isEqualTo(
                    oppdaterVedtaksforslagRequest.engangsbeløpListe[0].kravhaver.toString(),
                )
            },
            Executable {
                assertThat(oppdatertVedtaksforslag.engangsbeløpListe[0].mottaker.toString()).isEqualTo(
                    oppdaterVedtaksforslagRequest.engangsbeløpListe[0].mottaker.toString(),
                )
            },
            Executable {
                assertThat(oppdatertVedtaksforslag.engangsbeløpListe[0].beløp?.toInt()).isEqualTo(
                    oppdaterVedtaksforslagRequest.engangsbeløpListe[0].beløp?.toInt(),
                )
            },
            Executable { assertThat(oppdatertVedtaksforslag.engangsbeløpListe[0].valutakode).isEqualTo(oppdaterVedtaksforslagRequest.engangsbeløpListe[0].valutakode) },
            Executable {
                assertThat(oppdatertVedtaksforslag.engangsbeløpListe[0].resultatkode)
                    .isEqualTo(oppdaterVedtaksforslagRequest.engangsbeløpListe[0].resultatkode)
            },
            Executable { assertThat(oppdatertVedtaksforslag.engangsbeløpListe[0].innkreving).isEqualTo(oppdaterVedtaksforslagRequest.engangsbeløpListe[0].innkreving) },
            Executable { assertThat(oppdatertVedtaksforslag.engangsbeløpListe[0].beslutning).isEqualTo(oppdaterVedtaksforslagRequest.engangsbeløpListe[0].beslutning) },
            Executable {
                assertThat(oppdatertVedtaksforslag.engangsbeløpListe[0].omgjørVedtakId).isEqualTo(
                    oppdaterVedtaksforslagRequest.engangsbeløpListe[0].omgjørVedtakId,
                )
            },
            Executable { assertThat(oppdatertVedtaksforslag.engangsbeløpListe[0].referanse).isEqualTo(oppdaterVedtaksforslagRequest.engangsbeløpListe[0].referanse) },
            Executable { assertThat(oppdatertVedtaksforslag.engangsbeløpListe[0].delytelseId).isEqualTo(oppdaterVedtaksforslagRequest.engangsbeløpListe[0].delytelseId) },
            Executable {
                assertThat(oppdatertVedtaksforslag.engangsbeløpListe[0].eksternReferanse).isEqualTo(
                    oppdaterVedtaksforslagRequest.engangsbeløpListe[0].eksternReferanse,
                )
            },
            Executable { assertThat(oppdatertVedtaksforslag.engangsbeløpListe[0].grunnlagReferanseListe.size).isEqualTo(1) },
            Executable {
                assertThat(oppdatertVedtaksforslag.engangsbeløpListe[0].grunnlagReferanseListe.size)
                    .isEqualTo(oppdaterVedtaksforslagRequest.engangsbeløpListe[0].grunnlagReferanseListe.size)
            },

        )
    }

    @Test
    fun `skal fatte vedtak fra vedtaksforslag`() {
        // Oppdaterer vedtaksforslag
        val vedtaksforslagRequest = byggVedtaksforslagMedOppdatertInnholdRequest()

        val vedtaksforslagVedtaksid = vedtakService.opprettVedtak(
            vedtakRequest = vedtaksforslagRequest,
            vedtaksforslag = true,
        ).vedtaksid

        // Henter oppdatert vedtaksforslag
        val opprettetVedtaksforslag = vedtakService.hentVedtak(vedtaksforslagVedtaksid)

        // Fatt vedtak fra vedtaksforslag
        val fattetVedtakVedtaksid = vedtakService.fattVedtakForVedtaksforslag(vedtaksforslagVedtaksid)

        // Henter oppdatert vedtaksforslag
        val vedtakFattetFraVedtaksforslag = vedtakService.hentVedtak(fattetVedtakVedtaksid)

        assertAll(
            // Vedtaksforslag

            // Vedtak
            Executable { assertThat(vedtakFattetFraVedtaksforslag.kilde).isEqualTo(vedtaksforslagRequest.kilde) },
            Executable { assertThat(vedtakFattetFraVedtaksforslag.type).isEqualTo(vedtaksforslagRequest.type) },
//      Executable { assertThat(oppdvedtakFattetFraVedtaksforslagtakstidspunkt).isEqualTo(nyttVedtakRequest.vedtakstidspunkt) },
            Executable { assertThat(vedtakFattetFraVedtaksforslag.opprettetTidspunkt).isNotNull() },
            Executable { assertThat(vedtakFattetFraVedtaksforslag.vedtakstidspunkt).isNotNull() },
            Executable { assertThat(vedtakFattetFraVedtaksforslag.opprettetAv).isEqualTo(vedtaksforslagRequest.opprettetAv) },
            Executable { assertThat(vedtakFattetFraVedtaksforslag.unikReferanse).isEqualTo(vedtaksforslagRequest.unikReferanse) },
            Executable { assertThat(vedtakFattetFraVedtaksforslag.enhetsnummer).isEqualTo(vedtaksforslagRequest.enhetsnummer) },
            Executable { assertThat(vedtakFattetFraVedtaksforslag.innkrevingUtsattTilDato).isEqualTo(vedtaksforslagRequest.innkrevingUtsattTilDato) },
            Executable { assertThat(vedtakFattetFraVedtaksforslag.fastsattILand).isEqualTo(vedtaksforslagRequest.fastsattILand) },
            Executable { assertThat(vedtakFattetFraVedtaksforslag.grunnlagListe.size).isEqualTo(1) },
            Executable { assertThat(vedtakFattetFraVedtaksforslag.stønadsendringListe.size).isEqualTo(1) },
            Executable { assertThat(vedtakFattetFraVedtaksforslag.behandlingsreferanseListe.size).isEqualTo(1) },

            // Grunnlag
            Executable { assertThat(vedtakFattetFraVedtaksforslag.grunnlagListe[0].referanse).isEqualTo(vedtaksforslagRequest.grunnlagListe[0].referanse) },
            Executable { assertThat(vedtakFattetFraVedtaksforslag.grunnlagListe[0].type).isEqualTo(vedtaksforslagRequest.grunnlagListe[0].type) },
            Executable { assertThat(vedtakFattetFraVedtaksforslag.grunnlagListe[0].innhold).isEqualTo(vedtaksforslagRequest.grunnlagListe[0].innhold) },
            Executable { assertThat(vedtakFattetFraVedtaksforslag.grunnlagListe[0].gjelderReferanse).isEqualTo("PERSON_BM") },
            Executable { assertThat(vedtakFattetFraVedtaksforslag.grunnlagListe[0].grunnlagsreferanseListe).isEmpty() },
            Executable { assertThat(vedtakFattetFraVedtaksforslag.grunnlagListe.size).isEqualTo(vedtaksforslagRequest.grunnlagListe.size) },
            // Stønadsendring
            Executable { assertThat(vedtakFattetFraVedtaksforslag.stønadsendringListe[0].type).isEqualTo(vedtaksforslagRequest.stønadsendringListe[0].type) },
            Executable { assertThat(vedtakFattetFraVedtaksforslag.stønadsendringListe[0].sak).isEqualTo(vedtaksforslagRequest.stønadsendringListe[0].sak) },
            Executable {
                assertThat(opprettetVedtaksforslag.stønadsendringListe[0].skyldner.toString()).isEqualTo(
                    vedtaksforslagRequest.stønadsendringListe[0].skyldner.toString(),
                )
            },
            Executable {
                assertThat(opprettetVedtaksforslag.stønadsendringListe[0].kravhaver.toString()).isEqualTo(
                    vedtaksforslagRequest.stønadsendringListe[0].kravhaver.toString(),
                )
            },
            Executable {
                assertThat(opprettetVedtaksforslag.stønadsendringListe[0].mottaker.toString()).isEqualTo(
                    vedtaksforslagRequest.stønadsendringListe[0].mottaker.toString(),
                )
            },
            Executable {
                assertThat(opprettetVedtaksforslag.stønadsendringListe[0].førsteIndeksreguleringsår).isEqualTo(
                    vedtaksforslagRequest.stønadsendringListe[0].førsteIndeksreguleringsår,
                )
            },
            Executable {
                assertThat(opprettetVedtaksforslag.stønadsendringListe[0].innkreving)
                    .isEqualTo(vedtaksforslagRequest.stønadsendringListe[0].innkreving)
            },
            Executable {
                assertThat(opprettetVedtaksforslag.stønadsendringListe[0].beslutning)
                    .isEqualTo(vedtaksforslagRequest.stønadsendringListe[0].beslutning)
            },
            Executable {
                assertThat(opprettetVedtaksforslag.stønadsendringListe[0].omgjørVedtakId).isEqualTo(
                    vedtaksforslagRequest.stønadsendringListe[0].omgjørVedtakId,
                )
            },
            Executable {
                assertThat(opprettetVedtaksforslag.stønadsendringListe[0].eksternReferanse).isEqualTo(
                    vedtaksforslagRequest.stønadsendringListe[0].eksternReferanse,
                )
            },
//            Executable {
//                assertThat(oppdatertVedtaksforslag.stønadsendringListe[0].grunnlagReferanseListe[0]).isEqualTo(
//                    oppdaterVedtaksforslagRequest.stønadsendringListe[0].grunnlagReferanseListe[0],
//                )
//            },

            Executable {
                assertThat(opprettetVedtaksforslag.stønadsendringListe.size)
                    .isEqualTo(vedtaksforslagRequest.stønadsendringListe.size)
            },

            Executable { assertThat(opprettetVedtaksforslag.stønadsendringListe[0].grunnlagReferanseListe.size).isEqualTo(0) },

            Executable {
                assertThat(opprettetVedtaksforslag.stønadsendringListe[0].grunnlagReferanseListe.size)
                    .isEqualTo(vedtaksforslagRequest.stønadsendringListe[0].grunnlagReferanseListe.size)
            },

            Executable { assertThat(opprettetVedtaksforslag.stønadsendringListe[0].periodeListe.size).isEqualTo(1) },

            Executable {
                assertThat(opprettetVedtaksforslag.stønadsendringListe[0].periodeListe.size)
                    .isEqualTo(vedtaksforslagRequest.stønadsendringListe[0].periodeListe.size)
            },

            // Periode
            Executable {
                assertThat(opprettetVedtaksforslag.stønadsendringListe[0].periodeListe[0].periode.fom).isEqualTo(
                    vedtaksforslagRequest.stønadsendringListe[0].periodeListe[0].periode.fom,
                )
            },
            Executable {
                assertThat(opprettetVedtaksforslag.stønadsendringListe[0].periodeListe[0].periode.til).isEqualTo(
                    vedtaksforslagRequest.stønadsendringListe[0].periodeListe[0].periode.til,
                )
            },
            Executable {
                assertThat(opprettetVedtaksforslag.stønadsendringListe[0].periodeListe[0].beløp?.toInt()).isEqualTo(
                    vedtaksforslagRequest.stønadsendringListe[0].periodeListe[0].beløp?.toInt(),
                )
            },
            Executable {
                assertThat(opprettetVedtaksforslag.stønadsendringListe[0].periodeListe[0].valutakode).isEqualTo(
                    vedtaksforslagRequest.stønadsendringListe[0].periodeListe[0].valutakode,
                )
            },
            Executable {
                assertThat(opprettetVedtaksforslag.stønadsendringListe[0].periodeListe[0].resultatkode).isEqualTo(
                    vedtaksforslagRequest.stønadsendringListe[0].periodeListe[0].resultatkode,
                )
            },
            Executable {
                assertThat(opprettetVedtaksforslag.stønadsendringListe[0].periodeListe[0].delytelseId).isEqualTo(
                    vedtaksforslagRequest.stønadsendringListe[0].periodeListe[0].delytelseId,
                )
            },
            Executable { assertThat(opprettetVedtaksforslag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(1) },

            Executable {
                assertThat(opprettetVedtaksforslag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size)
                    .isEqualTo(vedtaksforslagRequest.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size)
            },

            // GrunnlagReferanse
            Executable {
                assertThat(opprettetVedtaksforslag.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo(
                    vedtaksforslagRequest.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0],
                )
            },
            // Engangsbeløp
            Executable { assertThat(opprettetVedtaksforslag.engangsbeløpListe.size).isEqualTo(1) },
            Executable {
                assertThat(opprettetVedtaksforslag.engangsbeløpListe.size)
                    .isEqualTo(vedtaksforslagRequest.engangsbeløpListe.size)
            },

            Executable { assertThat(opprettetVedtaksforslag.engangsbeløpListe[0].type).isEqualTo(vedtaksforslagRequest.engangsbeløpListe[0].type) },
            Executable { assertThat(opprettetVedtaksforslag.engangsbeløpListe[0].sak).isEqualTo(vedtaksforslagRequest.engangsbeløpListe[0].sak) },
            Executable {
                assertThat(opprettetVedtaksforslag.engangsbeløpListe[0].skyldner.toString()).isEqualTo(
                    vedtaksforslagRequest.engangsbeløpListe[0].skyldner.toString(),
                )
            },
            Executable {
                assertThat(opprettetVedtaksforslag.engangsbeløpListe[0].kravhaver.toString()).isEqualTo(
                    vedtaksforslagRequest.engangsbeløpListe[0].kravhaver.toString(),
                )
            },
            Executable {
                assertThat(opprettetVedtaksforslag.engangsbeløpListe[0].mottaker.toString()).isEqualTo(
                    vedtaksforslagRequest.engangsbeløpListe[0].mottaker.toString(),
                )
            },
            Executable {
                assertThat(opprettetVedtaksforslag.engangsbeløpListe[0].beløp?.toInt()).isEqualTo(
                    vedtaksforslagRequest.engangsbeløpListe[0].beløp?.toInt(),
                )
            },
            Executable { assertThat(opprettetVedtaksforslag.engangsbeløpListe[0].valutakode).isEqualTo(vedtaksforslagRequest.engangsbeløpListe[0].valutakode) },
            Executable {
                assertThat(opprettetVedtaksforslag.engangsbeløpListe[0].resultatkode)
                    .isEqualTo(vedtaksforslagRequest.engangsbeløpListe[0].resultatkode)
            },
            Executable { assertThat(opprettetVedtaksforslag.engangsbeløpListe[0].innkreving).isEqualTo(vedtaksforslagRequest.engangsbeløpListe[0].innkreving) },
            Executable { assertThat(opprettetVedtaksforslag.engangsbeløpListe[0].beslutning).isEqualTo(vedtaksforslagRequest.engangsbeløpListe[0].beslutning) },
            Executable {
                assertThat(opprettetVedtaksforslag.engangsbeløpListe[0].omgjørVedtakId).isEqualTo(
                    vedtaksforslagRequest.engangsbeløpListe[0].omgjørVedtakId,
                )
            },
            Executable { assertThat(opprettetVedtaksforslag.engangsbeløpListe[0].referanse).isEqualTo(vedtaksforslagRequest.engangsbeløpListe[0].referanse) },
            Executable { assertThat(opprettetVedtaksforslag.engangsbeløpListe[0].delytelseId).isEqualTo(vedtaksforslagRequest.engangsbeløpListe[0].delytelseId) },
            Executable {
                assertThat(opprettetVedtaksforslag.engangsbeløpListe[0].eksternReferanse).isEqualTo(
                    vedtaksforslagRequest.engangsbeløpListe[0].eksternReferanse,
                )
            },
            Executable { assertThat(opprettetVedtaksforslag.engangsbeløpListe[0].grunnlagReferanseListe.size).isEqualTo(1) },
            Executable {
                assertThat(opprettetVedtaksforslag.engangsbeløpListe[0].grunnlagReferanseListe.size)
                    .isEqualTo(vedtaksforslagRequest.engangsbeløpListe[0].grunnlagReferanseListe.size)
            },

        )
    }

    @Test
    fun `skal slette vedtaksforslag`() {
        // Oppdaterer vedtaksforslag
        val vedtaksforslagRequest = byggVedtaksforslagMedOppdatertInnholdRequest()

        val vedtaksforslagVedtaksid = vedtakService.opprettVedtak(
            vedtakRequest = vedtaksforslagRequest,
            vedtaksforslag = true,
        ).vedtaksid

        // Henter oppdatert vedtaksforslag
        vedtakService.hentVedtak(vedtaksforslagVedtaksid)

        // Fatt vedtak fra vedtaksforslag
        val slettetVedtaksforslagVedtaksid = vedtakService.slettVedtaksforslag(vedtaksforslagVedtaksid)

        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            vedtakService.hentVedtak(slettetVedtaksforslagVedtaksid)
        }
    }
}
