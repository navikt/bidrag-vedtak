package no.nav.bidrag.vedtak.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.bidrag.commons.service.organisasjon.SaksbehandlernavnProvider
import no.nav.bidrag.domene.enums.vedtak.Beslutningstype
import no.nav.bidrag.domene.enums.vedtak.Engangsbeløptype
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.vedtak.BidragVedtakTest
import no.nav.bidrag.vedtak.TestUtil.Companion.byggBehandlingsreferanse
import no.nav.bidrag.vedtak.TestUtil.Companion.byggEngangsbeløp
import no.nav.bidrag.vedtak.TestUtil.Companion.byggEngangsbeløpGrunnlag
import no.nav.bidrag.vedtak.TestUtil.Companion.byggGrunnlag
import no.nav.bidrag.vedtak.TestUtil.Companion.byggPeriode
import no.nav.bidrag.vedtak.TestUtil.Companion.byggPeriodeGrunnlag
import no.nav.bidrag.vedtak.TestUtil.Companion.byggStønadsendring
import no.nav.bidrag.vedtak.TestUtil.Companion.byggStønadsendringGrunnlag
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtak
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtakForOppdateringRequest
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtakRequest
import no.nav.bidrag.vedtak.bo.EngangsbeløpGrunnlagBo
import no.nav.bidrag.vedtak.bo.PeriodeGrunnlagBo
import no.nav.bidrag.vedtak.bo.StønadsendringGrunnlagBo
import no.nav.bidrag.vedtak.persistence.entity.Behandlingsreferanse
import no.nav.bidrag.vedtak.persistence.entity.Engangsbeløp
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import no.nav.bidrag.vedtak.persistence.entity.Periode
import no.nav.bidrag.vedtak.persistence.entity.Stønadsendring
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import no.nav.bidrag.vedtak.util.IdentUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal

@DisplayName("VedtakServiceMockTest")
@ActiveProfiles(BidragVedtakTest.TEST_PROFILE)
@ExtendWith(MockitoExtension::class)
class VedtakServiceMockTest {

    @InjectMocks
    private lateinit var vedtakService: VedtakService

    @Mock
    private lateinit var hendelserService: HendelserService

    @Mock
    private lateinit var identUtilsMock: IdentUtils

    @Spy
    private var meterRegistry: MeterRegistry = SimpleMeterRegistry()

    @Mock
    private lateinit var persistenceServiceMock: PersistenceService

    @Captor
    private lateinit var vedtakCaptor: ArgumentCaptor<Vedtak>

    @Captor
    private lateinit var vedtaksidCaptor: ArgumentCaptor<Int>

    @Captor
    private lateinit var stønadsendringCaptor: ArgumentCaptor<Stønadsendring>

    @Captor
    private lateinit var stønadsendringsidCaptor: ArgumentCaptor<Int>

    @Captor
    private lateinit var stønadsendringGrunnlagBoCaptor: ArgumentCaptor<StønadsendringGrunnlagBo>

    @Captor
    private lateinit var engangsbeløpCaptor: ArgumentCaptor<Engangsbeløp>

    @Captor
    private lateinit var periodeCaptor: ArgumentCaptor<Periode>

    @Captor
    private lateinit var grunnlagCaptor: ArgumentCaptor<Grunnlag>

    @Captor
    private lateinit var periodeGrunnlagBoCaptor: ArgumentCaptor<PeriodeGrunnlagBo>

    @Captor
    private lateinit var engangsbeløpGrunnlagBoCaptor: ArgumentCaptor<EngangsbeløpGrunnlagBo>

    @Captor
    private lateinit var behandlingsreferanseCaptor: ArgumentCaptor<Behandlingsreferanse>

    @Captor
    private lateinit var personidentCaptor: ArgumentCaptor<Personident>

    @Test
    fun `skal opprette nytt vedtak`() {
        mockkObject(SaksbehandlernavnProvider)
        every { SaksbehandlernavnProvider.hentSaksbehandlernavn(any()) } returns "Saksbehandler Saksbehandlersen"

        Mockito.`when`(persistenceServiceMock.opprettVedtak(MockitoHelper.capture(vedtakCaptor)))
            .thenReturn(byggVedtak())
        Mockito.`when`(persistenceServiceMock.opprettStønadsendring(MockitoHelper.capture(stønadsendringCaptor)))
            .thenReturn(byggStønadsendring())
        Mockito.`when`(persistenceServiceMock.opprettStønadsendringGrunnlag(MockitoHelper.capture(stønadsendringGrunnlagBoCaptor)))
            .thenReturn(byggStønadsendringGrunnlag())
        Mockito.`when`(persistenceServiceMock.opprettEngangsbeløp(MockitoHelper.capture(engangsbeløpCaptor)))
            .thenReturn(byggEngangsbeløp())
        Mockito.`when`(persistenceServiceMock.opprettPeriode(MockitoHelper.capture(periodeCaptor)))
            .thenReturn(byggPeriode())
        Mockito.`when`(persistenceServiceMock.opprettGrunnlag(MockitoHelper.capture(grunnlagCaptor)))
            .thenReturn(byggGrunnlag())
        Mockito.`when`(persistenceServiceMock.opprettPeriodeGrunnlag(MockitoHelper.capture(periodeGrunnlagBoCaptor)))
            .thenReturn(byggPeriodeGrunnlag())
        Mockito.`when`(persistenceServiceMock.opprettEngangsbeløpGrunnlag(MockitoHelper.capture(engangsbeløpGrunnlagBoCaptor)))
            .thenReturn(byggEngangsbeløpGrunnlag())
        Mockito.`when`(persistenceServiceMock.opprettBehandlingsreferanse(MockitoHelper.capture(behandlingsreferanseCaptor)))
            .thenReturn(byggBehandlingsreferanse())
        Mockito.`when`(persistenceServiceMock.referanseErUnik(vedtaksid = any(), referanse = any())).thenReturn(true)

        Mockito.`when`(persistenceServiceMock.hentVedtak(MockitoHelper.capture(vedtaksidCaptor)))
            .thenReturn(byggVedtak())
        Mockito.`when`(persistenceServiceMock.hentAlleGrunnlagForVedtak(MockitoHelper.capture(vedtaksidCaptor)))
            .thenReturn(listOf(byggGrunnlag()))
        Mockito.`when`(persistenceServiceMock.hentAlleStønadsendringerForVedtak(MockitoHelper.capture(vedtaksidCaptor)))
            .thenReturn(listOf(byggStønadsendring()))
        Mockito.`when`(persistenceServiceMock.hentAlleEngangsbeløpForVedtak(MockitoHelper.capture(vedtaksidCaptor)))
            .thenReturn(listOf(byggEngangsbeløp()))
        Mockito.`when`(persistenceServiceMock.hentAlleBehandlingsreferanserForVedtak(MockitoHelper.capture(vedtaksidCaptor)))
            .thenReturn(listOf(byggBehandlingsreferanse()))

        val opprettVedtakRequestDto = byggVedtakRequest()
        val nyttVedtakOpprettet = vedtakService.opprettVedtak(opprettVedtakRequestDto, false)

        val vedtak = vedtakCaptor.value
        val stønadsendringListe = stønadsendringCaptor.allValues
        val stønadsendringGrunnlagBoListe = stønadsendringGrunnlagBoCaptor.allValues
        val engangsbeløpListe = engangsbeløpCaptor.allValues
        val periodeListe = periodeCaptor.allValues
        val grunnlagListe = grunnlagCaptor.allValues
        val periodeGrunnlagBoListe = periodeGrunnlagBoCaptor.allValues
        val engangsbeløpGrunnlagBoListe = engangsbeløpGrunnlagBoCaptor.allValues
        val behandlingsreferanseListe = behandlingsreferanseCaptor.allValues

        Mockito.verify(
            persistenceServiceMock,
            Mockito.times(1),
        ).opprettVedtak(MockitoHelper.any(Vedtak::class.java))
        Mockito.verify(
            persistenceServiceMock,
            Mockito.times(2),
        ).opprettStønadsendring(MockitoHelper.any(Stønadsendring::class.java))
        Mockito.verify(
            persistenceServiceMock,
            Mockito.times(4),
        ).opprettStønadsendringGrunnlag(
            MockitoHelper.any(StønadsendringGrunnlagBo::class.java),
        )
        Mockito.verify(
            persistenceServiceMock,
            Mockito.times(3),
        ).opprettEngangsbeløp(MockitoHelper.any(Engangsbeløp::class.java))
        Mockito.verify(
            persistenceServiceMock,
            Mockito.times(4),
        ).opprettPeriode(MockitoHelper.any(Periode::class.java))
        Mockito.verify(
            persistenceServiceMock,
            Mockito.times(8),
        ).opprettGrunnlag(MockitoHelper.any(Grunnlag::class.java))
        Mockito.verify(
            persistenceServiceMock,
            Mockito.times(11),
        ).opprettPeriodeGrunnlag(MockitoHelper.any(PeriodeGrunnlagBo::class.java))
        Mockito.verify(
            persistenceServiceMock,
            Mockito.times(9),
        ).opprettEngangsbeløpGrunnlag(MockitoHelper.any(EngangsbeløpGrunnlagBo::class.java))
        Mockito.verify(
            persistenceServiceMock,
            Mockito.times(2),
        ).opprettBehandlingsreferanse(MockitoHelper.any(Behandlingsreferanse::class.java))

        assertAll(
            { assertThat(nyttVedtakOpprettet).isNotNull() },

            // Sjekk VedtakDto
            { assertThat(vedtak).isNotNull() },
            { assertThat(vedtak.type).isEqualTo(opprettVedtakRequestDto.type.toString()) },
            { assertThat(vedtak.enhetsnummer).isEqualTo(opprettVedtakRequestDto.enhetsnummer.toString()) },
            { assertThat(vedtak.opprettetAv).isEqualTo(opprettVedtakRequestDto.opprettetAv) },
            { assertThat(vedtak.vedtakstidspunkt).isEqualTo(opprettVedtakRequestDto.vedtakstidspunkt) },
            { assertThat(vedtak.innkrevingUtsattTilDato).isEqualTo(opprettVedtakRequestDto.innkrevingUtsattTilDato) },

            // Sjekk StønadsendringDto
            { assertThat(stønadsendringListe).isNotNull() },
            { assertThat(stønadsendringListe.size).isEqualTo(2) },

            { assertThat(stønadsendringListe[0].type).isEqualTo(opprettVedtakRequestDto.stønadsendringListe[0].type.toString()) },
            { assertThat(stønadsendringListe[0].sak).isEqualTo(opprettVedtakRequestDto.stønadsendringListe[0].sak.toString()) },
            { assertThat(stønadsendringListe[0].skyldner).isEqualTo(opprettVedtakRequestDto.stønadsendringListe[0].skyldner.verdi) },
            { assertThat(stønadsendringListe[0].kravhaver).isEqualTo(opprettVedtakRequestDto.stønadsendringListe[0].kravhaver.verdi) },
            { assertThat(stønadsendringListe[0].mottaker).isEqualTo(opprettVedtakRequestDto.stønadsendringListe[0].mottaker.verdi) },
            {
                assertThat(stønadsendringListe[0].innkreving).isEqualTo(
                    opprettVedtakRequestDto.stønadsendringListe[0].innkreving.toString(),
                )
            },
            {
                assertThat(stønadsendringListe[0].beslutning).isEqualTo(
                    opprettVedtakRequestDto.stønadsendringListe[0].beslutning.toString(),
                )
            },

            { assertThat(stønadsendringListe[1].type).isEqualTo(opprettVedtakRequestDto.stønadsendringListe[1].type.toString()) },
            { assertThat(stønadsendringListe[1].sak).isEqualTo(opprettVedtakRequestDto.stønadsendringListe[1].sak.toString()) },
            { assertThat(stønadsendringListe[1].skyldner).isEqualTo(opprettVedtakRequestDto.stønadsendringListe[1].skyldner.verdi) },
            { assertThat(stønadsendringListe[1].kravhaver).isEqualTo(opprettVedtakRequestDto.stønadsendringListe[1].kravhaver.verdi) },
            { assertThat(stønadsendringListe[1].mottaker).isEqualTo(opprettVedtakRequestDto.stønadsendringListe[1].mottaker.verdi) },
            {
                assertThat(stønadsendringListe[1].innkreving).isEqualTo(
                    opprettVedtakRequestDto.stønadsendringListe[1].innkreving.toString(),
                )
            },
            {
                assertThat(stønadsendringListe[1].beslutning).isEqualTo(
                    opprettVedtakRequestDto.stønadsendringListe[1].beslutning.toString(),
                )
            },

            // Sjekk StønadsendringGrunnlagDto
            { assertThat(stønadsendringGrunnlagBoListe).isNotNull() },
            { assertThat(stønadsendringGrunnlagBoListe.size).isEqualTo(4) },

            // Sjekk EngangsbeløpDto
            { assertThat(engangsbeløpListe).isNotNull() },
            { assertThat(engangsbeløpListe.size).isEqualTo(3) },

            { assertThat(engangsbeløpListe[0].type).isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![0].type.toString()) },
            { assertThat(engangsbeløpListe[0].sak).isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![0].sak.toString()) },
            { assertThat(engangsbeløpListe[0].skyldner).isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![0].skyldner.verdi) },
            { assertThat(engangsbeløpListe[0].kravhaver).isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![0].kravhaver.verdi) },
            { assertThat(engangsbeløpListe[0].mottaker).isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![0].mottaker.verdi) },
            { assertThat(engangsbeløpListe[0].beløp).isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![0].beløp) },
            { assertThat(engangsbeløpListe[0].valutakode).isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![0].valutakode) },
            { assertThat(engangsbeløpListe[0].resultatkode).isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![0].resultatkode) },
            { assertThat(engangsbeløpListe[0].referanse).isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![0].referanse) },
            {
                assertThat(engangsbeløpListe[0].innkreving)
                    .isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![0].innkreving.toString())
            },
            {
                assertThat(engangsbeløpListe[0].beslutning)
                    .isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![0].beslutning.toString())
            },

            { assertThat(engangsbeløpListe[1].type).isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![1].type.toString()) },
            { assertThat(engangsbeløpListe[1].sak).isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![1].sak.toString()) },
            { assertThat(engangsbeløpListe[1].skyldner).isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![1].skyldner.verdi) },
            { assertThat(engangsbeløpListe[1].kravhaver).isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![1].kravhaver.verdi) },
            { assertThat(engangsbeløpListe[1].mottaker).isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![1].mottaker.verdi) },
            { assertThat(engangsbeløpListe[1].beløp).isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![1].beløp) },
            { assertThat(engangsbeløpListe[1].valutakode).isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![1].valutakode) },
            { assertThat(engangsbeløpListe[1].resultatkode).isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![1].resultatkode) },
            { assertThat(engangsbeløpListe[1].referanse).isNotNull() },
            {
                assertThat(engangsbeløpListe[1].innkreving)
                    .isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![1].innkreving.toString())
            },
            {
                assertThat(engangsbeløpListe[1].beslutning)
                    .isEqualTo(opprettVedtakRequestDto.engangsbeløpListe!![1].beslutning.toString())
            },

            // Sjekk PeriodeDto
            { assertThat(periodeListe).isNotNull() },
            { assertThat(periodeListe.size).isEqualTo(4) },

            {
                assertThat(periodeListe[0].fom).isEqualTo(
                    opprettVedtakRequestDto.stønadsendringListe[0].periodeListe[0].periode.toDatoperiode().fom,
                )
            },
            {
                assertThat(periodeListe[0].til).isEqualTo(
                    opprettVedtakRequestDto.stønadsendringListe[0].periodeListe[0].periode.toDatoperiode().til,
                )
            },
            { assertThat(periodeListe[0].beløp).isEqualTo(opprettVedtakRequestDto.stønadsendringListe[0].periodeListe[0].beløp) },
            {
                assertThat(periodeListe[0].valutakode)
                    .isEqualTo(opprettVedtakRequestDto.stønadsendringListe[0].periodeListe[0].valutakode)
            },
            {
                assertThat(periodeListe[0].resultatkode).isEqualTo(
                    opprettVedtakRequestDto.stønadsendringListe[0].periodeListe[0].resultatkode,
                )
            },

            {
                assertThat(periodeListe[1].fom).isEqualTo(
                    opprettVedtakRequestDto.stønadsendringListe[0].periodeListe[1].periode.toDatoperiode().fom,
                )
            },
            {
                assertThat(periodeListe[1].til).isEqualTo(
                    opprettVedtakRequestDto.stønadsendringListe[0].periodeListe[1].periode.toDatoperiode().til,
                )
            },
            { assertThat(periodeListe[1].beløp).isEqualTo(opprettVedtakRequestDto.stønadsendringListe[0].periodeListe[1].beløp) },
            {
                assertThat(periodeListe[1].valutakode)
                    .isEqualTo(opprettVedtakRequestDto.stønadsendringListe[0].periodeListe[1].valutakode)
            },
            {
                assertThat(periodeListe[1].resultatkode).isEqualTo(
                    opprettVedtakRequestDto.stønadsendringListe[0].periodeListe[1].resultatkode,
                )
            },

            {
                assertThat(periodeListe[2].fom).isEqualTo(
                    opprettVedtakRequestDto.stønadsendringListe[1].periodeListe[0].periode.toDatoperiode().fom,
                )
            },
            {
                assertThat(periodeListe[2].til).isEqualTo(
                    opprettVedtakRequestDto.stønadsendringListe[1].periodeListe[0].periode.toDatoperiode().til,
                )
            },
            { assertThat(periodeListe[2].beløp).isEqualTo(opprettVedtakRequestDto.stønadsendringListe[1].periodeListe[0].beløp) },
            {
                assertThat(periodeListe[2].valutakode)
                    .isEqualTo(opprettVedtakRequestDto.stønadsendringListe[1].periodeListe[0].valutakode)
            },
            {
                assertThat(periodeListe[2].resultatkode).isEqualTo(
                    opprettVedtakRequestDto.stønadsendringListe[1].periodeListe[0].resultatkode,
                )
            },

            {
                assertThat(periodeListe[3].fom).isEqualTo(
                    opprettVedtakRequestDto.stønadsendringListe[1].periodeListe[1].periode.toDatoperiode().fom,
                )
            },
            {
                assertThat(periodeListe[3].til).isEqualTo(
                    opprettVedtakRequestDto.stønadsendringListe[1].periodeListe[1].periode.toDatoperiode().til,
                )
            },
            { assertThat(periodeListe[3].beløp).isEqualTo(opprettVedtakRequestDto.stønadsendringListe[1].periodeListe[1].beløp) },
            {
                assertThat(periodeListe[3].valutakode)
                    .isEqualTo(opprettVedtakRequestDto.stønadsendringListe[1].periodeListe[1].valutakode)
            },
            {
                assertThat(periodeListe[3].resultatkode).isEqualTo(
                    opprettVedtakRequestDto.stønadsendringListe[1].periodeListe[1].resultatkode,
                )
            },

            // Sjekk GrunnlagDto
            { assertThat(grunnlagListe).isNotNull() },
            { assertThat(grunnlagListe.size).isEqualTo(8) },

            { assertThat(grunnlagListe[0].referanse).isEqualTo(opprettVedtakRequestDto.grunnlagListe[0].referanse) },
            { assertThat(grunnlagListe[0].type).isEqualTo(opprettVedtakRequestDto.grunnlagListe[0].type.toString()) },
            { assertThat(grunnlagListe[0].innhold).isEqualTo(opprettVedtakRequestDto.grunnlagListe[0].innhold.toString()) },

            { assertThat(grunnlagListe[1].referanse).isEqualTo(opprettVedtakRequestDto.grunnlagListe[1].referanse) },
            { assertThat(grunnlagListe[1].type).isEqualTo(opprettVedtakRequestDto.grunnlagListe[1].type.toString()) },
            { assertThat(grunnlagListe[1].innhold).isEqualTo(opprettVedtakRequestDto.grunnlagListe[1].innhold.toString()) },

            { assertThat(grunnlagListe[2].referanse).isEqualTo(opprettVedtakRequestDto.grunnlagListe[2].referanse) },
            { assertThat(grunnlagListe[2].type).isEqualTo(opprettVedtakRequestDto.grunnlagListe[2].type.toString()) },
            { assertThat(grunnlagListe[2].innhold).isEqualTo(opprettVedtakRequestDto.grunnlagListe[2].innhold.toString()) },

            { assertThat(grunnlagListe[3].referanse).isEqualTo(opprettVedtakRequestDto.grunnlagListe[3].referanse) },
            { assertThat(grunnlagListe[3].type).isEqualTo(opprettVedtakRequestDto.grunnlagListe[3].type.toString()) },
            { assertThat(grunnlagListe[3].innhold).isEqualTo(opprettVedtakRequestDto.grunnlagListe[3].innhold.toString()) },

            // Sjekk PeriodeGrunnlagDto
            { assertThat(periodeGrunnlagBoListe).isNotNull() },
            { assertThat(periodeGrunnlagBoListe.size).isEqualTo(11) },

            // Sjekk EngangsbeløpGrunnlagDto
            { assertThat(engangsbeløpGrunnlagBoListe).isNotNull() },
            { assertThat(engangsbeløpGrunnlagBoListe.size).isEqualTo(9) },

            // Sjekk BehandlingsreferanseDto
            { assertThat(behandlingsreferanseListe).isNotNull() },
            { assertThat(behandlingsreferanseListe.size).isEqualTo(2) },

        )

        // Rydder bort mock
        unmockkObject(SaksbehandlernavnProvider)
    }

    @Test
    fun `skal hente vedtak`() {
        // Hent vedtak
        Mockito.`when`(persistenceServiceMock.hentVedtak(MockitoHelper.any(Int::class.java))).thenReturn(byggVedtak(vedtaksid = 1))
        Mockito.`when`(persistenceServiceMock.hentAlleGrunnlagForVedtak(MockitoHelper.any(Int::class.java)))
            .thenReturn(
                listOf(
                    byggGrunnlag(grunnlagsid = 1, vedtak = byggVedtak(), grunnlagReferanse = "REF1"),
                    byggGrunnlag(grunnlagsid = 2, vedtak = byggVedtak(), grunnlagReferanse = "REF2"),
                ),
            )
        Mockito.`when`(persistenceServiceMock.hentAlleStønadsendringerForVedtak(MockitoHelper.any(Int::class.java))).thenReturn(
            listOf(
                byggStønadsendring(stønadsendringsid = 1),
                byggStønadsendring(stønadsendringsid = 2),
            ),
        )
        Mockito.`when`(persistenceServiceMock.hentAlleEngangsbeløpForVedtak(MockitoHelper.any(Int::class.java))).thenReturn(
            listOf(
                byggEngangsbeløp(
                    engangsbeløpId = 1,
                    type = "SÆRBIDRAG",
                    sak = Saksnummer("SAK-101").toString(),
                    skyldner = Personident("01018011111").toString(),
                    kravhaver = Personident("01010511111").toString(),
                    mottaker = Personident("01018211111").toString(),
                    beløp = BigDecimal.valueOf(
                        3490,
                    ),
                    valutakode = "NOK",
                    resultatkode = "SAERTILSKUDD BEREGNET",
                    Innkrevingstype.MED_INNKREVING,
                    Beslutningstype.ENDRING,
                    omgjørVedtakId = 123,
                    referanse = "Referanse1",
                    delytelseId = "delytelseId1",
                    eksternReferanse = "EksternRef1",
                ),
            ),
        )
        Mockito.`when`(persistenceServiceMock.hentAllePerioderForStønadsendring(MockitoHelper.any(Int::class.java)))
            .thenReturn(
                listOf(
                    byggPeriode(periodeid = 1, beløp = BigDecimal.valueOf(100)),
                    byggPeriode(periodeid = 2, beløp = BigDecimal.valueOf(200)),
                ),
            )
        Mockito.`when`(persistenceServiceMock.hentAlleGrunnlagForPeriode(MockitoHelper.any(Int::class.java))).thenReturn(
            listOf(
                byggPeriodeGrunnlag(periode = byggPeriode(periodeid = 1), grunnlag = byggGrunnlag(grunnlagsid = 1)),
                byggPeriodeGrunnlag(periode = byggPeriode(periodeid = 2), grunnlag = byggGrunnlag(grunnlagsid = 2)),
            ),
        )
        Mockito.`when`(persistenceServiceMock.hentGrunnlag(MockitoHelper.any(Int::class.java))).thenReturn(
            byggGrunnlag(grunnlagsid = 1, vedtak = byggVedtak(), grunnlagReferanse = "REF1"),
        )
        Mockito.`when`(persistenceServiceMock.hentAlleGrunnlagForEngangsbeløp(MockitoHelper.any(Int::class.java))).thenReturn(
            listOf(
                byggEngangsbeløpGrunnlag(engangsbeløp = byggEngangsbeløp(engangsbeløpId = 1), grunnlag = byggGrunnlag(grunnlagsid = 1)),
                byggEngangsbeløpGrunnlag(engangsbeløp = byggEngangsbeløp(engangsbeløpId = 2), grunnlag = byggGrunnlag(grunnlagsid = 2)),
            ),
        )
        Mockito.`when`(persistenceServiceMock.hentAlleBehandlingsreferanserForVedtak(MockitoHelper.any(Int::class.java))).thenReturn(
            listOf(
                byggBehandlingsreferanse(kilde = "BISYS_SØKNAD", referanse = "Bisys-01"),
                byggBehandlingsreferanse(kilde = "BISYS_SØKNAD", referanse = "Bisys-02"),
            ),
        )

        val vedtakFunnet = vedtakService.hentVedtak(1)

        assertAll(
            { assertThat(vedtakFunnet).isNotNull() },
            { assertThat(vedtakFunnet.kilde).isEqualTo(Vedtakskilde.MANUELT) },
            { assertThat(vedtakFunnet.type).isEqualTo(Vedtakstype.ALDERSJUSTERING) },
            { assertThat(vedtakFunnet.grunnlagListe.size).isEqualTo(2) },
            { assertThat(vedtakFunnet.grunnlagListe[0].referanse).isEqualTo("REF1") },
            { assertThat(vedtakFunnet.stønadsendringListe.size).isEqualTo(2) },
            { assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe.size).isEqualTo(2) },
            { assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].beløp).isEqualTo(BigDecimal.valueOf(100)) },
            { assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(2) },
            { assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo("REF1") },
            { assertThat(vedtakFunnet.engangsbeløpListe.size).isEqualTo(1) },
            { assertThat(vedtakFunnet.engangsbeløpListe[0].type).isEqualTo(Engangsbeløptype.SÆRBIDRAG) },
            { assertThat(vedtakFunnet.engangsbeløpListe[0].sak.toString()).isEqualTo("SAK-101") },
            { assertThat(vedtakFunnet.behandlingsreferanseListe.size).isEqualTo(2) },
        )
    }

    @Test
    fun `skal oppdatere vedtak`() {
        mockkObject(SaksbehandlernavnProvider)
        mockkObject(IdentUtils)
        every { SaksbehandlernavnProvider.hentSaksbehandlernavn(any()) } returns "Saksbehandler Saksbehandlersen"

        Mockito.`when`(identUtilsMock.hentNyesteIdent(MockitoHelper.capture(personidentCaptor)))
            .thenReturn(Personident("12345678901"))

        Mockito.`when`(persistenceServiceMock.opprettVedtak(MockitoHelper.capture(vedtakCaptor)))
            .thenReturn(byggVedtak())
        Mockito.`when`(persistenceServiceMock.opprettStønadsendring(MockitoHelper.capture(stønadsendringCaptor)))
            .thenReturn(byggStønadsendring())
        Mockito.`when`(persistenceServiceMock.opprettPeriode(MockitoHelper.capture(periodeCaptor)))
            .thenReturn(byggPeriode())
        Mockito.`when`(persistenceServiceMock.opprettGrunnlag(MockitoHelper.capture(grunnlagCaptor)))
            .thenReturn(byggGrunnlag())
        Mockito.`when`(persistenceServiceMock.hentVedtak(MockitoHelper.capture(vedtaksidCaptor)))
            .thenReturn(byggVedtak())
        Mockito.`when`(persistenceServiceMock.hentAlleGrunnlagForVedtak(MockitoHelper.capture(vedtaksidCaptor)))
            .thenReturn(listOf(byggGrunnlag()))
        Mockito.`when`(persistenceServiceMock.hentAlleStønadsendringerForVedtak(MockitoHelper.capture(vedtaksidCaptor)))
            .thenReturn(listOf(byggStønadsendring()))
        Mockito.`when`(persistenceServiceMock.hentAllePerioderForStønadsendring(MockitoHelper.capture(stønadsendringsidCaptor)))
            .thenReturn(listOf(byggPeriode()))
        Mockito.`when`(persistenceServiceMock.hentAlleEngangsbeløpForVedtak(MockitoHelper.capture(vedtaksidCaptor)))
            .thenReturn(emptyList())
        Mockito.`when`(persistenceServiceMock.hentAlleBehandlingsreferanserForVedtak(MockitoHelper.capture(vedtaksidCaptor)))
            .thenReturn(emptyList())

        val opprettVedtakRequestDto = byggVedtakForOppdateringRequest()
        val nyttVedtakOpprettet = vedtakService.opprettVedtak(opprettVedtakRequestDto, false)

        vedtakService.oppdaterVedtak(nyttVedtakOpprettet.vedtaksid, opprettVedtakRequestDto)

        // Rydder bort mock
        unmockkObject(SaksbehandlernavnProvider)
    }

    object MockitoHelper {

        // use this in place of captor.capture() if you are trying to capture an argument that is not nullable
        fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

        fun <T> any(type: Class<T>): T = Mockito.any(type)
    }
}
