package no.nav.bidrag.vedtak.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.bidrag.domene.enums.Beslutningstype
import no.nav.bidrag.domene.enums.Engangsbeløptype
import no.nav.bidrag.domene.enums.Vedtakskilde
import no.nav.bidrag.domene.enums.Vedtakstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.streng.Saksnummer
import no.nav.bidrag.vedtak.TestUtil.Companion.byggBehandlingsreferanse
import no.nav.bidrag.vedtak.TestUtil.Companion.byggEngangsbeløp
import no.nav.bidrag.vedtak.TestUtil.Companion.byggEngangsbeløpGrunnlag
import no.nav.bidrag.vedtak.TestUtil.Companion.byggGrunnlag
import no.nav.bidrag.vedtak.TestUtil.Companion.byggPeriode
import no.nav.bidrag.vedtak.TestUtil.Companion.byggPeriodeGrunnlag
import no.nav.bidrag.vedtak.TestUtil.Companion.byggStønadsendring
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtak
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtakRequest
import no.nav.bidrag.vedtak.bo.EngangsbeløpGrunnlagBo
import no.nav.bidrag.vedtak.bo.PeriodeGrunnlagBo
import no.nav.bidrag.vedtak.persistence.entity.Behandlingsreferanse
import no.nav.bidrag.vedtak.persistence.entity.Engangsbeløp
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import no.nav.bidrag.vedtak.persistence.entity.Periode
import no.nav.bidrag.vedtak.persistence.entity.Stønadsendring
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal

@DisplayName("VedtakServiceMockTest")
@ExtendWith(MockitoExtension::class)
class VedtakServiceMockTest {

    @InjectMocks
    private lateinit var vedtakService: VedtakService

    @Mock
    private lateinit var hendelserService: HendelserService

    @Spy
    private var meterRegistry: MeterRegistry = SimpleMeterRegistry()

    @Mock
    private lateinit var persistenceServiceMock: PersistenceService

    @Captor
    private lateinit var vedtakCaptor: ArgumentCaptor<Vedtak>

    @Captor
    private lateinit var stønadsendringCaptor: ArgumentCaptor<Stønadsendring>

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

    @Test
    fun `skal opprette nytt vedtak`() {
        Mockito.`when`(persistenceServiceMock.opprettVedtak(MockitoHelper.capture(vedtakCaptor)))
            .thenReturn(byggVedtak())
        Mockito.`when`(persistenceServiceMock.opprettStønadsendring(MockitoHelper.capture(stønadsendringCaptor)))
            .thenReturn(byggStønadsendring())
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

        val vedtak = byggVedtakRequest()
        val nyttVedtakOpprettet = vedtakService.opprettVedtak(vedtak)

        val vedtakDto = vedtakCaptor.value
        val stønadsendringDtoListe = stønadsendringCaptor.allValues
        val engangsbeløpDtoListe = engangsbeløpCaptor.allValues
        val periodeDtoListe = periodeCaptor.allValues
        val grunnlagDtoListe = grunnlagCaptor.allValues
        val periodeGrunnlagBoListe = periodeGrunnlagBoCaptor.allValues
        val engangsbeløpGrunnlagBoListe = engangsbeløpGrunnlagBoCaptor.allValues
        val behandlingsreferanseListe = behandlingsreferanseCaptor.allValues

        Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettVedtak(MockitoHelper.any(Vedtak::class.java))
        Mockito.verify(persistenceServiceMock, Mockito.times(2)).opprettStønadsendring(MockitoHelper.any(Stønadsendring::class.java))
        Mockito.verify(persistenceServiceMock, Mockito.times(2)).opprettEngangsbeløp(MockitoHelper.any(Engangsbeløp::class.java))
        Mockito.verify(persistenceServiceMock, Mockito.times(4)).opprettPeriode(MockitoHelper.any(Periode::class.java))
        Mockito.verify(persistenceServiceMock, Mockito.times(4)).opprettGrunnlag(MockitoHelper.any(Grunnlag::class.java))
        Mockito.verify(persistenceServiceMock, Mockito.times(11)).opprettPeriodeGrunnlag(MockitoHelper.any(PeriodeGrunnlagBo::class.java))
        Mockito.verify(persistenceServiceMock, Mockito.times(6)).opprettEngangsbeløpGrunnlag(MockitoHelper.any(EngangsbeløpGrunnlagBo::class.java))
        Mockito.verify(persistenceServiceMock, Mockito.times(2)).opprettBehandlingsreferanse(MockitoHelper.any(Behandlingsreferanse::class.java))

        assertAll(
            Executable { assertThat(nyttVedtakOpprettet).isNotNull() },

            // Sjekk VedtakDto
            Executable { assertThat(vedtakDto).isNotNull() },
            Executable { assertThat(vedtakDto.type).isEqualTo(vedtak.type.toString()) },
            Executable { assertThat(vedtakDto.enhetsnummer).isEqualTo(vedtak.enhetsnummer) },
            Executable { assertThat(vedtakDto.opprettetAv).isEqualTo(vedtak.opprettetAv) },
            Executable { assertThat(vedtakDto.opprettetAvNavn).isEqualTo(vedtak.opprettetAvNavn) },
            Executable { assertThat(vedtakDto.vedtakstidspunkt).isEqualTo(vedtak.vedtakstidspunkt) },
            Executable { assertThat(vedtakDto.innkrevingUtsattTilDato).isEqualTo(vedtak.innkrevingUtsattTilDato) },

            // Sjekk StønadsendringDto
            Executable { assertThat(stønadsendringDtoListe).isNotNull() },
            Executable { assertThat(stønadsendringDtoListe.size).isEqualTo(2) },

            Executable { assertThat(stønadsendringDtoListe[0].type).isEqualTo(vedtak.stønadsendringListe!![0].type.toString()) },
            Executable { assertThat(stønadsendringDtoListe[0].sak).isEqualTo(vedtak.stønadsendringListe!![0].sak) },
            Executable { assertThat(stønadsendringDtoListe[0].skyldner).isEqualTo(vedtak.stønadsendringListe!![0].skyldner) },
            Executable { assertThat(stønadsendringDtoListe[0].kravhaver).isEqualTo(vedtak.stønadsendringListe!![0].kravhaver) },
            Executable { assertThat(stønadsendringDtoListe[0].mottaker).isEqualTo(vedtak.stønadsendringListe!![0].mottaker) },
            Executable { assertThat(stønadsendringDtoListe[0].innkreving).isEqualTo(vedtak.stønadsendringListe!![0].innkreving.toString()) },
            Executable { assertThat(stønadsendringDtoListe[0].beslutning).isEqualTo(vedtak.stønadsendringListe!![0].beslutning) },

            Executable { assertThat(stønadsendringDtoListe[1].type).isEqualTo(vedtak.stønadsendringListe!![1].type.toString()) },
            Executable { assertThat(stønadsendringDtoListe[1].sak).isEqualTo(vedtak.stønadsendringListe!![1].sak) },
            Executable { assertThat(stønadsendringDtoListe[1].skyldner).isEqualTo(vedtak.stønadsendringListe!![1].skyldner) },
            Executable { assertThat(stønadsendringDtoListe[1].kravhaver).isEqualTo(vedtak.stønadsendringListe!![1].kravhaver) },
            Executable { assertThat(stønadsendringDtoListe[1].mottaker).isEqualTo(vedtak.stønadsendringListe!![1].mottaker) },
            Executable { assertThat(stønadsendringDtoListe[1].innkreving).isEqualTo(vedtak.stønadsendringListe!![1].innkreving.toString()) },
            Executable { assertThat(stønadsendringDtoListe[1].beslutning).isEqualTo(vedtak.stønadsendringListe!![1].beslutning) },

            // Sjekk EngangsbeløpDto
            Executable { assertThat(engangsbeløpDtoListe).isNotNull() },
            Executable { assertThat(engangsbeløpDtoListe.size).isEqualTo(2) },

            Executable { assertThat(engangsbeløpDtoListe[0].type).isEqualTo(vedtak.engangsbeløpListe!![0].type.toString()) },
            Executable { assertThat(engangsbeløpDtoListe[0].sak).isEqualTo(vedtak.engangsbeløpListe!![0].sak) },
            Executable { assertThat(engangsbeløpDtoListe[0].skyldner).isEqualTo(vedtak.engangsbeløpListe!![0].skyldner) },
            Executable { assertThat(engangsbeløpDtoListe[0].kravhaver).isEqualTo(vedtak.engangsbeløpListe!![0].kravhaver) },
            Executable { assertThat(engangsbeløpDtoListe[0].mottaker).isEqualTo(vedtak.engangsbeløpListe!![0].mottaker) },
            Executable { assertThat(engangsbeløpDtoListe[0].beløp).isEqualTo(vedtak.engangsbeløpListe!![0].beløp) },
            Executable { assertThat(engangsbeløpDtoListe[0].valutakode).isEqualTo(vedtak.engangsbeløpListe!![0].valutakode) },
            Executable { assertThat(engangsbeløpDtoListe[0].resultatkode).isEqualTo(vedtak.engangsbeløpListe!![0].resultatkode) },
            Executable { assertThat(engangsbeløpDtoListe[0].referanse).isEqualTo(vedtak.engangsbeløpListe!![0].referanse) },
            Executable { assertThat(engangsbeløpDtoListe[0].innkreving).isEqualTo(vedtak.engangsbeløpListe!![0].innkreving.toString()) },
            Executable { assertThat(engangsbeløpDtoListe[0].beslutning).isEqualTo(vedtak.engangsbeløpListe!![0].beslutning) },

            Executable { assertThat(engangsbeløpDtoListe[1].type).isEqualTo(vedtak.engangsbeløpListe!![1].type.toString()) },
            Executable { assertThat(engangsbeløpDtoListe[1].sak).isEqualTo(vedtak.engangsbeløpListe!![1].sak) },
            Executable { assertThat(engangsbeløpDtoListe[1].skyldner).isEqualTo(vedtak.engangsbeløpListe!![1].skyldner) },
            Executable { assertThat(engangsbeløpDtoListe[1].kravhaver).isEqualTo(vedtak.engangsbeløpListe!![1].kravhaver) },
            Executable { assertThat(engangsbeløpDtoListe[1].mottaker).isEqualTo(vedtak.engangsbeløpListe!![1].mottaker) },
            Executable { assertThat(engangsbeløpDtoListe[1].beløp).isEqualTo(vedtak.engangsbeløpListe!![1].beløp) },
            Executable { assertThat(engangsbeløpDtoListe[1].valutakode).isEqualTo(vedtak.engangsbeløpListe!![1].valutakode) },
            Executable { assertThat(engangsbeløpDtoListe[1].resultatkode).isEqualTo(vedtak.engangsbeløpListe!![1].resultatkode) },
            Executable { assertThat(engangsbeløpDtoListe[1].referanse).isEqualTo(vedtak.engangsbeløpListe!![1].referanse) },
            Executable { assertThat(engangsbeløpDtoListe[1].innkreving).isEqualTo(vedtak.engangsbeløpListe!![1].innkreving.toString()) },
            Executable { assertThat(engangsbeløpDtoListe[1].beslutning).isEqualTo(vedtak.engangsbeløpListe!![1].beslutning) },

            // Sjekk PeriodeDto
            Executable { assertThat(periodeDtoListe).isNotNull() },
            Executable { assertThat(periodeDtoListe.size).isEqualTo(4) },

            Executable { assertThat(periodeDtoListe[0].fomDato).isEqualTo(vedtak.stønadsendringListe!![0].periodeListe[0].periode.fomDato) },
            Executable { assertThat(periodeDtoListe[0].tilDato).isEqualTo(vedtak.stønadsendringListe!![0].periodeListe[0].periode.tilDato) },
            Executable { assertThat(periodeDtoListe[0].beløp).isEqualTo(vedtak.stønadsendringListe!![0].periodeListe[0].beløp) },
            Executable { assertThat(periodeDtoListe[0].valutakode).isEqualTo(vedtak.stønadsendringListe!![0].periodeListe[0].valutakode) },
            Executable { assertThat(periodeDtoListe[0].resultatkode).isEqualTo(vedtak.stønadsendringListe!![0].periodeListe[0].resultatkode) },

            Executable { assertThat(periodeDtoListe[1].fomDato).isEqualTo(vedtak.stønadsendringListe!![0].periodeListe[1].periode.fomDato) },
            Executable { assertThat(periodeDtoListe[1].tilDato).isEqualTo(vedtak.stønadsendringListe!![0].periodeListe[1].periode.tilDato) },
            Executable { assertThat(periodeDtoListe[1].beløp).isEqualTo(vedtak.stønadsendringListe!![0].periodeListe[1].beløp) },
            Executable { assertThat(periodeDtoListe[1].valutakode).isEqualTo(vedtak.stønadsendringListe!![0].periodeListe[1].valutakode) },
            Executable { assertThat(periodeDtoListe[1].resultatkode).isEqualTo(vedtak.stønadsendringListe!![0].periodeListe[1].resultatkode) },

            Executable { assertThat(periodeDtoListe[2].fomDato).isEqualTo(vedtak.stønadsendringListe!![1].periodeListe[0].periode.fomDato) },
            Executable { assertThat(periodeDtoListe[2].tilDato).isEqualTo(vedtak.stønadsendringListe!![1].periodeListe[0].periode.tilDato) },
            Executable { assertThat(periodeDtoListe[2].beløp).isEqualTo(vedtak.stønadsendringListe!![1].periodeListe[0].beløp) },
            Executable { assertThat(periodeDtoListe[2].valutakode).isEqualTo(vedtak.stønadsendringListe!![1].periodeListe[0].valutakode) },
            Executable { assertThat(periodeDtoListe[2].resultatkode).isEqualTo(vedtak.stønadsendringListe!![1].periodeListe[0].resultatkode) },

            Executable { assertThat(periodeDtoListe[3].fomDato).isEqualTo(vedtak.stønadsendringListe!![1].periodeListe[1].periode.fomDato) },
            Executable { assertThat(periodeDtoListe[3].tilDato).isEqualTo(vedtak.stønadsendringListe!![1].periodeListe[1].periode.tilDato) },
            Executable { assertThat(periodeDtoListe[3].beløp).isEqualTo(vedtak.stønadsendringListe!![1].periodeListe[1].beløp) },
            Executable { assertThat(periodeDtoListe[3].valutakode).isEqualTo(vedtak.stønadsendringListe!![1].periodeListe[1].valutakode) },
            Executable { assertThat(periodeDtoListe[3].resultatkode).isEqualTo(vedtak.stønadsendringListe!![1].periodeListe[1].resultatkode) },

            // Sjekk GrunnlagDto
            Executable { assertThat(grunnlagDtoListe).isNotNull() },
            Executable { assertThat(grunnlagDtoListe.size).isEqualTo(4) },

            Executable { assertThat(grunnlagDtoListe[0].referanse).isEqualTo(vedtak.grunnlagListe[0].referanse) },
            Executable { assertThat(grunnlagDtoListe[0].type).isEqualTo(vedtak.grunnlagListe[0].type.toString()) },
            Executable { assertThat(grunnlagDtoListe[0].innhold).isEqualTo(vedtak.grunnlagListe[0].innhold.toString()) },

            Executable { assertThat(grunnlagDtoListe[1].referanse).isEqualTo(vedtak.grunnlagListe[1].referanse) },
            Executable { assertThat(grunnlagDtoListe[1].type).isEqualTo(vedtak.grunnlagListe[1].type.toString()) },
            Executable { assertThat(grunnlagDtoListe[1].innhold).isEqualTo(vedtak.grunnlagListe[1].innhold.toString()) },

            Executable { assertThat(grunnlagDtoListe[2].referanse).isEqualTo(vedtak.grunnlagListe[2].referanse) },
            Executable { assertThat(grunnlagDtoListe[2].type).isEqualTo(vedtak.grunnlagListe[2].type.toString()) },
            Executable { assertThat(grunnlagDtoListe[2].innhold).isEqualTo(vedtak.grunnlagListe[2].innhold.toString()) },

            Executable { assertThat(grunnlagDtoListe[3].referanse).isEqualTo(vedtak.grunnlagListe[3].referanse) },
            Executable { assertThat(grunnlagDtoListe[3].type).isEqualTo(vedtak.grunnlagListe[3].type.toString()) },
            Executable { assertThat(grunnlagDtoListe[3].innhold).isEqualTo(vedtak.grunnlagListe[3].innhold.toString()) },

            // Sjekk PeriodeGrunnlagDto
            Executable { assertThat(periodeGrunnlagBoListe).isNotNull() },
            Executable { assertThat(periodeGrunnlagBoListe.size).isEqualTo(11) },

            // Sjekk EngangsbeløpGrunnlagDto
            Executable { assertThat(engangsbeløpGrunnlagBoListe).isNotNull() },
            Executable { assertThat(engangsbeløpGrunnlagBoListe.size).isEqualTo(6) },

            // Sjekk BehandlingsreferanseDto
            Executable { assertThat(behandlingsreferanseListe).isNotNull() },
            Executable { assertThat(behandlingsreferanseListe.size).isEqualTo(2) }

        )
    }

    @Test
    fun `skal hente vedtak`() {
        // Hent vedtak
        Mockito.`when`(persistenceServiceMock.hentVedtak(MockitoHelper.any(Int::class.java))).thenReturn(byggVedtak(vedtakId = 1))
        Mockito.`when`(persistenceServiceMock.hentAlleGrunnlagForVedtak(MockitoHelper.any(Int::class.java)))
            .thenReturn(
                listOf(
                    byggGrunnlag(grunnlagId = 1, vedtak = byggVedtak(), grunnlagReferanse = "REF1"),
                    byggGrunnlag(grunnlagId = 2, vedtak = byggVedtak(), grunnlagReferanse = "REF2")
                )
            )
        Mockito.`when`(persistenceServiceMock.hentAlleStønadsendringerForVedtak(MockitoHelper.any(Int::class.java))).thenReturn(
            listOf(
                byggStønadsendring(stønadsendringId = 1),
                byggStønadsendring(stønadsendringId = 2)
            )
        )
        Mockito.`when`(persistenceServiceMock.hentAlleEngangsbeløpForVedtak(MockitoHelper.any(Int::class.java))).thenReturn(
            listOf(
                byggEngangsbeløp(
                    engangsbeløpId = 1, type = "SAERTILSKUDD", sak = Saksnummer("SAK-101").toString(), skyldner = Personident("01018011111").toString(),
                    kravhaver = Personident("01010511111").toString(), mottaker = Personident("01018211111").toString(), beløp = BigDecimal.valueOf(3490), valutakode = "NOK",
                    resultatkode = "SAERTILSKUDD BEREGNET", innkreving = "JA", Beslutningstype.ENDRING, omgjørVedtakId = 123, referanse = "Referanse1",
                    delytelseId = "delytelseId1", eksternReferanse = "EksternRef1"
                )
            )
        )
        Mockito.`when`(persistenceServiceMock.hentAllePerioderForStønadsendring(MockitoHelper.any(Int::class.java)))
            .thenReturn(
                listOf(
                    byggPeriode(periodeId = 1, beløp = BigDecimal.valueOf(100)),
                    byggPeriode(periodeId = 2, beløp = BigDecimal.valueOf(200))
                )
            )
        Mockito.`when`(persistenceServiceMock.hentAlleGrunnlagForPeriode(MockitoHelper.any(Int::class.java))).thenReturn(
            listOf(
                byggPeriodeGrunnlag(periode = byggPeriode(periodeId = 1), grunnlag = byggGrunnlag(grunnlagId = 1)),
                byggPeriodeGrunnlag(periode = byggPeriode(periodeId = 2), grunnlag = byggGrunnlag(grunnlagId = 2))
            )
        )
        Mockito.`when`(persistenceServiceMock.hentGrunnlag(MockitoHelper.any(Int::class.java))).thenReturn(
            byggGrunnlag(grunnlagId = 1, vedtak = byggVedtak(), grunnlagReferanse = "REF1")
        )
        Mockito.`when`(persistenceServiceMock.hentAlleGrunnlagForEngangsbeløp(MockitoHelper.any(Int::class.java))).thenReturn(
            listOf(
                byggEngangsbeløpGrunnlag(engangsbeløp = byggEngangsbeløp(engangsbeløpId = 1), grunnlag = byggGrunnlag(grunnlagId = 1)),
                byggEngangsbeløpGrunnlag(engangsbeløp = byggEngangsbeløp(engangsbeløpId = 2), grunnlag = byggGrunnlag(grunnlagId = 2))
            )
        )
        Mockito.`when`(persistenceServiceMock.hentAlleBehandlingsreferanserForVedtak(MockitoHelper.any(Int::class.java))).thenReturn(
            listOf(
                byggBehandlingsreferanse(kilde = "BISYS_SOKNAD", referanse = "Bisys-01"),
                byggBehandlingsreferanse(kilde = "BISYS_SOKNAD", referanse = "Bisys-02")
            )
        )

        val vedtakFunnet = vedtakService.hentVedtak(1)

        assertAll(
            Executable { assertThat(vedtakFunnet).isNotNull() },
            Executable { assertThat(vedtakFunnet.kilde).isEqualTo(Vedtakskilde.MANUELT) },
            Executable { assertThat(vedtakFunnet.type).isEqualTo(Vedtakstype.ALDERSJUSTERING) },
            Executable { assertThat(vedtakFunnet.grunnlagListe.size).isEqualTo(2) },
            Executable { assertThat(vedtakFunnet.grunnlagListe[0].referanse).isEqualTo("REF1") },
            Executable { assertThat(vedtakFunnet.stønadsendringListe.size).isEqualTo(2) },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe.size).isEqualTo(2) },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].beløp).isEqualTo(BigDecimal.valueOf(100)) },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(2) },
            Executable { assertThat(vedtakFunnet.stønadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo("REF1") },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe.size).isEqualTo(1) },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[0].type).isEqualTo(Engangsbeløptype.SAERTILSKUDD) },
            Executable { assertThat(vedtakFunnet.engangsbeløpListe[0].sak).isEqualTo("SAK-101") },
            Executable { assertThat(vedtakFunnet.behandlingsreferanseListe.size).isEqualTo(2) }
        )
    }

    object MockitoHelper {

        // use this in place of captor.capture() if you are trying to capture an argument that is not nullable
        fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

        fun <T> any(type: Class<T>): T = Mockito.any(type)
    }
}
