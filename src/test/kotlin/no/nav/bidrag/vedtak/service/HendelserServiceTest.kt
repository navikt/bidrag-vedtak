package no.nav.bidrag.vedtak.service

import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.domene.enums.vedtak.BehandlingsrefKilde
import no.nav.bidrag.domene.enums.vedtak.Beslutningstype
import no.nav.bidrag.domene.enums.vedtak.Engangsbeløptype
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.VedtaksforslagStatus
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.organisasjon.Enhetsnummer
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.vedtak.Behandlingsreferanse
import no.nav.bidrag.transport.behandling.vedtak.Engangsbeløp
import no.nav.bidrag.transport.behandling.vedtak.Periode
import no.nav.bidrag.transport.behandling.vedtak.Sporingsdata
import no.nav.bidrag.transport.behandling.vedtak.Stønadsendring
import no.nav.bidrag.transport.behandling.vedtak.VedtakHendelse
import no.nav.bidrag.transport.behandling.vedtak.VedtaksforslagHendelse
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettBehandlingsreferanseRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettPeriodeRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettStønadsendringRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettVedtakRequestDto
import no.nav.bidrag.transport.behandling.vedtak.response.BehandlingsreferanseDto
import no.nav.bidrag.transport.behandling.vedtak.response.EngangsbeløpDto
import no.nav.bidrag.transport.behandling.vedtak.response.StønadsendringDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakPeriodeDto
import no.nav.bidrag.vedtak.BidragVedtakTest
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.anyOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@DisplayName("HendelserServiceTest")
@ActiveProfiles(BidragVedtakTest.TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakTest::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
class HendelserServiceTest {

    @Autowired
    private lateinit var hendelserService: HendelserService

    @MockitoBean
    private lateinit var vedtakEventProducerMock: VedtakKafkaEventProducer

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal opprette hendelse når kun engangsbeløp er del av request`() {
        hendelserService.opprettHendelseVedtak(
            VedtakDto(
                kilde = Vedtakskilde.MANUELT,
                type = Vedtakstype.ALDERSJUSTERING,
                opprettetAv = "ABCDEFG",
                opprettetAvNavn = "Random Person",
                kildeapplikasjon = "bidrag-behandling",
                vedtakstidspunkt = LocalDateTime.now(),
                unikReferanse = null,
                enhetsnummer = Enhetsnummer("ABCD"),
                innkrevingUtsattTilDato = LocalDate.now(),
                fastsattILand = null,
                opprettetTidspunkt = LocalDateTime.now(),
                grunnlagListe = emptyList(),
                stønadsendringListe = emptyList(),
                engangsbeløpListe = listOf(
                    EngangsbeløpDto(
                        type = Engangsbeløptype.SÆRBIDRAG, sak = Saksnummer("sak01"), skyldner = Personident("D"),
                        kravhaver = Personident("E"),
                        mottaker = Personident("F"), beløp = BigDecimal.ONE, valutakode = "NOK", resultatkode = "A",
                        innkreving = Innkrevingstype.MED_INNKREVING,
                        beslutning = Beslutningstype.ENDRING, omgjørVedtakId = null, referanse = "referanse1", delytelseId = null,
                        eksternReferanse = null,
                        grunnlagReferanseListe = listOf("A"),
                    ),
                ),
                behandlingsreferanseListe = emptyList(),
            ),
            vedtakId = 1,
        )

        verify(vedtakEventProducerMock).publishVedtak(anyOrNull())
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal opprette en hendelse når kun stønadsendring er del av request`() {
        hendelserService.opprettHendelseVedtak(
            VedtakDto(
                kilde = Vedtakskilde.MANUELT,
                type = Vedtakstype.ALDERSJUSTERING,
                opprettetAv = "ABCDEFG",
                opprettetAvNavn = "Random Person",
                kildeapplikasjon = "bidrag-behandling",
                vedtakstidspunkt = LocalDateTime.now(),
                unikReferanse = null,
                enhetsnummer = Enhetsnummer("ABCD"),
                innkrevingUtsattTilDato = LocalDate.now(),
                fastsattILand = null,
                opprettetTidspunkt = LocalDateTime.now(),
                grunnlagListe = emptyList(),
                stønadsendringListe = listOf(
                    StønadsendringDto(
                        type = Stønadstype.BIDRAG, sak = Saksnummer("B"), skyldner = Personident("C"), kravhaver = Personident("D"),
                        mottaker = Personident(
                            "E",
                        ),
                        sisteVedtaksid = null,
                        førsteIndeksreguleringsår = 2024,
                        innkreving = Innkrevingstype.MED_INNKREVING,
                        beslutning = Beslutningstype.ENDRING,
                        omgjørVedtakId = null, eksternReferanse = null,
                        grunnlagReferanseListe = emptyList(),
                        periodeListe = listOf(
                            VedtakPeriodeDto(
                                periode = ÅrMånedsperiode(LocalDate.now(), LocalDate.now()),
                                beløp = BigDecimal.ONE,
                                valutakode = "NOK",
                                resultatkode = "A",
                                delytelseId = null,
                                grunnlagReferanseListe = listOf("A"),
                            ),
                        ),
                    ),
                ),
                engangsbeløpListe = emptyList(),
                behandlingsreferanseListe = emptyList(),
            ),
            vedtakId = 1,
        )

        verify(vedtakEventProducerMock).publishVedtak(anyOrNull())
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal opprette hendelse når både stønadsendring og engangsbeløp er del av request`() {
        hendelserService.opprettHendelseVedtak(
            VedtakDto(
                kilde = Vedtakskilde.MANUELT,
                type = Vedtakstype.ALDERSJUSTERING,
                opprettetAv = "ABCDEFG",
                opprettetAvNavn = "Random Person",
                kildeapplikasjon = "bidrag-behandling",
                vedtakstidspunkt = LocalDateTime.now(),
                unikReferanse = null,
                enhetsnummer = Enhetsnummer("ABCD"),
                innkrevingUtsattTilDato = LocalDate.now(),
                fastsattILand = null,
                opprettetTidspunkt = LocalDateTime.now(),
                grunnlagListe = emptyList(),
                stønadsendringListe = listOf(
                    StønadsendringDto(
                        type = Stønadstype.BIDRAG, sak = Saksnummer("B"), skyldner = Personident("C"), kravhaver = Personident("D"),
                        mottaker = Personident(
                            "E",
                        ),
                        sisteVedtaksid = null,
                        førsteIndeksreguleringsår = 2024, innkreving = Innkrevingstype.MED_INNKREVING,
                        beslutning = Beslutningstype.ENDRING,
                        omgjørVedtakId = null,
                        eksternReferanse = null,
                        grunnlagReferanseListe = emptyList(),
                        periodeListe = listOf(
                            VedtakPeriodeDto(
                                periode = ÅrMånedsperiode(LocalDate.now(), LocalDate.now()),
                                beløp = BigDecimal.ONE,
                                valutakode = "NOK",
                                resultatkode = "A",
                                delytelseId = null,
                                listOf("A"),
                            ),
                        ),
                    ),
                ),
                engangsbeløpListe = listOf(
                    EngangsbeløpDto(
                        type = Engangsbeløptype.SÆRBIDRAG,
                        sak = Saksnummer("sak01"),
                        skyldner = Personident("D"),
                        kravhaver = Personident("E"),
                        mottaker = Personident("F"),
                        beløp = BigDecimal.ONE,
                        valutakode = "NOK",
                        resultatkode = "A",
                        innkreving = Innkrevingstype.MED_INNKREVING,
                        beslutning = Beslutningstype.ENDRING,
                        omgjørVedtakId = null,
                        referanse = "referanse1",
                        delytelseId = null,
                        eksternReferanse = null,
                        grunnlagReferanseListe = listOf("A"),
                    ),
                ),
                behandlingsreferanseListe = emptyList(),
            ),
            1,
        )

        verify(vedtakEventProducerMock).publishVedtak(anyOrNull())
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal opprette en hendelse med skyldner-id`() {
        CorrelationId.existing("test")
        hendelserService.opprettHendelseVedtak(
            VedtakDto(
                kilde = Vedtakskilde.MANUELT,
                type = Vedtakstype.ALDERSJUSTERING,
                opprettetAv = "ABCDEFG",
                opprettetAvNavn = "Random Person",
                kildeapplikasjon = "bidrag-behandling",
                vedtakstidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
                unikReferanse = null,
                enhetsnummer = Enhetsnummer("ABCD"),
                innkrevingUtsattTilDato = LocalDate.now(),
                fastsattILand = "NO",
                opprettetTidspunkt = LocalDateTime.parse("2021-07-06T09:31:25.007971200"),
                grunnlagListe = emptyList(),
                stønadsendringListe = listOf(
                    StønadsendringDto(
                        type = Stønadstype.BIDRAG, sak = Saksnummer("B"), skyldner = Personident("C"),
                        kravhaver = Personident("D"),
                        mottaker = Personident("E"),
                        sisteVedtaksid = null,
                        førsteIndeksreguleringsår = 2024,
                        innkreving = Innkrevingstype.MED_INNKREVING,
                        beslutning = Beslutningstype.ENDRING,
                        omgjørVedtakId = null,
                        eksternReferanse = null,
                        grunnlagReferanseListe = emptyList(),
                        periodeListe = listOf(
                            VedtakPeriodeDto(
                                periode = ÅrMånedsperiode(LocalDate.now(), LocalDate.now()),
                                beløp = BigDecimal.ONE,
                                valutakode = "NOK",
                                "A",
                                "delytelseId1",
                                listOf("A"),
                            ),
                        ),
                    ),
                ),
                engangsbeløpListe = emptyList(),
                behandlingsreferanseListe = listOf(
                    BehandlingsreferanseDto(
                        kilde = BehandlingsrefKilde.BISYS_SØKNAD,
                        referanse = "referanse1",
                    ),
                ),
            ),
            vedtakId = 1,
        )

        verify(vedtakEventProducerMock).publishVedtak(
            VedtakHendelse(
                kilde = Vedtakskilde.MANUELT,
                type = Vedtakstype.ALDERSJUSTERING,
                id = 1,
                opprettetAv = "ABCDEFG",
                opprettetAvNavn = "Random Person",
                kildeapplikasjon = "bidrag-behandling",
                vedtakstidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
                enhetsnummer = Enhetsnummer("ABCD"),
                innkrevingUtsattTilDato = LocalDate.now(),
                fastsattILand = "NO",
                opprettetTidspunkt = LocalDateTime.parse("2021-07-06T09:31:25.007971200"),
                stønadsendringListe = listOf(
                    Stønadsendring(
                        type = Stønadstype.BIDRAG,
                        sak = Saksnummer("B"),
                        skyldner = Personident("C"),
                        kravhaver = Personident("D"),
                        mottaker = Personident("E"),
                        førsteIndeksreguleringsår = 2024,
                        innkreving = Innkrevingstype.MED_INNKREVING,
                        Beslutningstype.ENDRING,
                        omgjørVedtakId = null,
                        eksternReferanse = null,
                        listOf(
                            Periode(
                                periode = ÅrMånedsperiode(LocalDate.now(), LocalDate.now()),
                                beløp = BigDecimal.valueOf(1),
                                valutakode = "NOK",
                                resultatkode = "A",
                                delytelseId = "delytelseId1",
                            ),
                        ),
                    ),
                ),
                engangsbeløpListe = emptyList(),
                behandlingsreferanseListe = listOf(
                    Behandlingsreferanse(
                        kilde = BehandlingsrefKilde.BISYS_SØKNAD.toString(),
                        referanse = "referanse1",
                    ),
                ),
                sporingsdata = Sporingsdata("test"),
            ),
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal opprette hendelse ved engangsbeløp SAERTILSKUDD`() {
        hendelserService.opprettHendelseVedtak(
            VedtakDto(
                kilde = Vedtakskilde.MANUELT,
                type = Vedtakstype.ALDERSJUSTERING,
                opprettetAv = "ABCDEFG",
                opprettetAvNavn = "Random Person",
                kildeapplikasjon = "bidrag-behandling",
                vedtakstidspunkt = LocalDateTime.now(),
                unikReferanse = null,
                enhetsnummer = Enhetsnummer("ABCD"),
                innkrevingUtsattTilDato = LocalDate.now(),
                fastsattILand = null,
                opprettetTidspunkt = LocalDateTime.now(),
                grunnlagListe = emptyList(),
                stønadsendringListe = emptyList(),
                engangsbeløpListe = listOf(
                    EngangsbeløpDto(
                        type = Engangsbeløptype.SÆRBIDRAG,
                        sak = Saksnummer("SAK-101"),
                        skyldner = Personident("skyldner"),
                        kravhaver = Personident("kravhaver"),
                        mottaker = Personident("mottaker"),
                        beløp = BigDecimal.ONE,
                        resultatkode = "all is well",
                        valutakode = "Nok",
                        innkreving = Innkrevingstype.MED_INNKREVING,
                        beslutning = Beslutningstype.ENDRING,
                        omgjørVedtakId = null,
                        referanse = "referanse1",
                        delytelseId = null,
                        eksternReferanse = null,
                        grunnlagReferanseListe = listOf("A"),
                    ),
                ),
                behandlingsreferanseListe = emptyList(),
            ),
            vedtakId = 1,
        )
        verify(vedtakEventProducerMock).publishVedtak(anyOrNull())
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `opprettet hendelse skal ha innhold fra engangsbeløpListe`() {
        CorrelationId.existing("test")
        hendelserService.opprettHendelseVedtak(
            VedtakDto(
                kilde = Vedtakskilde.MANUELT,
                type = Vedtakstype.ALDERSJUSTERING,
                opprettetAv = "ABCDEFG",
                opprettetAvNavn = "Random Person",
                kildeapplikasjon = "bidrag-behandling",
                vedtakstidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
                unikReferanse = null,
                enhetsnummer = Enhetsnummer("ABCD"),
                innkrevingUtsattTilDato = LocalDate.now(),
                fastsattILand = "NO",
                opprettetTidspunkt = LocalDateTime.parse("2021-07-06T09:31:25.007971200"),
                grunnlagListe = emptyList(),
                stønadsendringListe = emptyList(),
                engangsbeløpListe = listOf(
                    EngangsbeløpDto(
                        type = Engangsbeløptype.SÆRBIDRAG,
                        sak = Saksnummer("SAK-101"),
                        skyldner = Personident("skyldner"),
                        kravhaver = Personident("kravhaver"),
                        mottaker = Personident("mottaker"),
                        beløp = BigDecimal.valueOf(2),
                        resultatkode = "all is well",
                        valutakode = "Nok",
                        innkreving = Innkrevingstype.MED_INNKREVING,
                        beslutning = Beslutningstype.ENDRING,
                        omgjørVedtakId = null,
                        referanse = "referanse1",
                        delytelseId = null,
                        eksternReferanse = null,
                        grunnlagReferanseListe = listOf("A"),
                    ),
                ),
                behandlingsreferanseListe = emptyList(),
            ),
            vedtakId = 1,
        )
        verify(vedtakEventProducerMock).publishVedtak(
            VedtakHendelse(
                kilde = Vedtakskilde.MANUELT,
                type = Vedtakstype.ALDERSJUSTERING,
                id = 1,
                vedtakstidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
                enhetsnummer = Enhetsnummer("ABCD"),
                innkrevingUtsattTilDato = LocalDate.now(),
                fastsattILand = "NO",
                opprettetAv = "ABCDEFG",
                opprettetAvNavn = "Random Person",
                kildeapplikasjon = "bidrag-behandling",
                opprettetTidspunkt = LocalDateTime.parse("2021-07-06T09:31:25.007971200"),
                stønadsendringListe = emptyList(),
                engangsbeløpListe =
                listOf(
                    Engangsbeløp(
                        type = Engangsbeløptype.SÆRBIDRAG,
                        sak = Saksnummer("SAK-101"),
                        skyldner = Personident("skyldner"),
                        kravhaver = Personident("kravhaver"),
                        mottaker = Personident("mottaker"),
                        beløp = BigDecimal.valueOf(2),
                        resultatkode = "all is well",
                        valutakode = "Nok",
                        innkreving = Innkrevingstype.MED_INNKREVING,
                        beslutning = Beslutningstype.ENDRING,
                        omgjørVedtakId = null,
                        referanse = "referanse1",
                        delytelseId = null,
                        eksternReferanse = null,
                    ),
                ),
                behandlingsreferanseListe = emptyList(),
                sporingsdata = Sporingsdata("test"),
            ),
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal opprette en hendelse for vedtaksforslag`() {
        CorrelationId.existing("test")
        hendelserService.opprettHendelseVedtaksforslag(
            status = VedtaksforslagStatus.OPPRETTET,
            request = OpprettVedtakRequestDto(
                kilde = Vedtakskilde.MANUELT,
                type = Vedtakstype.ALDERSJUSTERING,
                opprettetAv = "ABCDEFG",
                vedtakstidspunkt = null,
                unikReferanse = "unikReferanse",
                enhetsnummer = Enhetsnummer("ABCD"),
                innkrevingUtsattTilDato = LocalDate.now(),
                fastsattILand = "NO",
                grunnlagListe = emptyList(),
                stønadsendringListe = listOf(
                    OpprettStønadsendringRequestDto(
                        type = Stønadstype.BIDRAG,
                        sak = Saksnummer("B"),
                        skyldner = Personident("C"),
                        kravhaver = Personident("D"),
                        mottaker = Personident("E"),
                        sisteVedtaksid = null,
                        førsteIndeksreguleringsår = 2024,
                        innkreving = Innkrevingstype.MED_INNKREVING,
                        beslutning = Beslutningstype.ENDRING,
                        omgjørVedtakId = null,
                        eksternReferanse = null,
                        grunnlagReferanseListe = emptyList(),
                        listOf(
                            OpprettPeriodeRequestDto(
                                periode = ÅrMånedsperiode(LocalDate.now(), LocalDate.now()),
                                beløp = BigDecimal.ONE,
                                valutakode = "NOK",
                                "A",
                                "delytelseId1",
                                listOf("A"),
                            ),
                        ),
                    ),
                ),
                engangsbeløpListe = emptyList(),
                behandlingsreferanseListe = listOf(
                    OpprettBehandlingsreferanseRequestDto(
                        kilde = BehandlingsrefKilde.BISYS_SØKNAD,
                        referanse = "referanse1",
                    ),
                ),
            ),
            vedtakId = 1,
            saksnummer = Saksnummer("1234567"),
        )

        verify(vedtakEventProducerMock).publishVedtaksforslag(
            VedtaksforslagHendelse(
                status = VedtaksforslagStatus.OPPRETTET,
                vedtaksid = 1,
                saksnummerListe = listOf(Saksnummer("B")),
                sporingsdata = Sporingsdata("test"),
            ),
        )
    }
}
