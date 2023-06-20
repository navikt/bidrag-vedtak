package no.nav.bidrag.vedtak.service

import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.domain.enums.BehandlingsrefKilde
import no.nav.bidrag.domain.enums.EngangsbelopType
import no.nav.bidrag.domain.enums.Innkreving
import no.nav.bidrag.domain.enums.StonadType
import no.nav.bidrag.domain.enums.VedtakKilde
import no.nav.bidrag.domain.enums.VedtakType
import no.nav.bidrag.transport.behandling.vedtak.Behandlingsreferanse
import no.nav.bidrag.transport.behandling.vedtak.Engangsbelop
import no.nav.bidrag.transport.behandling.vedtak.Periode
import no.nav.bidrag.transport.behandling.vedtak.Sporingsdata
import no.nav.bidrag.transport.behandling.vedtak.Stonadsendring
import no.nav.bidrag.transport.behandling.vedtak.VedtakHendelse
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettBehandlingsreferanseRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettEngangsbelopRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettStonadsendringRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettVedtakPeriodeRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettVedtakRequestDto
import no.nav.bidrag.vedtak.BidragVedtakTest
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.anyOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
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

    @MockBean
    private lateinit var vedtakEventProducerMock: VedtakKafkaEventProducer

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal opprette hendelse når kun engangsbelop er del av request`() {
        hendelserService.opprettHendelse(
            OpprettVedtakRequestDto(
                kilde = VedtakKilde.MANUELT,
                type = VedtakType.ALDERSJUSTERING,
                opprettetAv = "ABCDEFG",
                opprettetAvNavn = "Saksbehandler1",
                vedtakTidspunkt = LocalDateTime.now(),
                enhetId = "ABCD",
                utsattTilDato = LocalDate.now(),
                grunnlagListe = emptyList(),
                stonadsendringListe = null,
                engangsbelopListe = listOf(
                    OpprettEngangsbelopRequestDto(
                        type = EngangsbelopType.SAERTILSKUDD, sakId = "sak01", skyldnerId = "D", kravhaverId = "E",
                        mottakerId = "F", belop = BigDecimal.ONE, valutakode = "NOK", resultatkode = "A", innkreving = Innkreving.JA,
                        endring = true, omgjorVedtakId = null, referanse = "referanse1", delytelseId = null, eksternReferanse = null,
                        listOf("A")
                    )
                ),
                behandlingsreferanseListe = null
            ),
            vedtakId = 1,
            opprettetTidspunkt = LocalDateTime.now()
        )

        verify(vedtakEventProducerMock).publish(anyOrNull())
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal opprette en hendelse når kun stønadsendring er del av request`() {
        hendelserService.opprettHendelse(
            OpprettVedtakRequestDto(
                kilde = VedtakKilde.MANUELT,
                type = VedtakType.ALDERSJUSTERING,
                opprettetAv = "ABCDEFG",
                opprettetAvNavn = "Saksbehandler1",
                vedtakTidspunkt = LocalDateTime.now(),
                enhetId = "ABCD",
                utsattTilDato = LocalDate.now(),
                grunnlagListe = emptyList(),
                stonadsendringListe = listOf(
                    OpprettStonadsendringRequestDto(
                        type = StonadType.BIDRAG, sakId = "B", skyldnerId = "C", kravhaverId = "D", mottakerId = "E",
                        indeksreguleringAar = "2024", innkreving = Innkreving.JA, endring = true,
                        omgjorVedtakId = null, eksternReferanse = null,
                        listOf(
                            OpprettVedtakPeriodeRequestDto(
                                fomDato = LocalDate.now(),
                                tilDato = LocalDate.now(),
                                belop = BigDecimal.ONE,
                                valutakode = "NOK",
                                resultatkode = "A",
                                delytelseId = null,
                                listOf("A")
                            )
                        )
                    )
                ),
                engangsbelopListe = emptyList(),
                behandlingsreferanseListe = emptyList()
            ),
            vedtakId = 1,
            opprettetTidspunkt = LocalDateTime.now()
        )

        verify(vedtakEventProducerMock).publish(anyOrNull())
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal opprette hendelse når både stonadsendring og engangsbelop er del av request`() {
        hendelserService.opprettHendelse(
            OpprettVedtakRequestDto(
                kilde = VedtakKilde.MANUELT,
                type = VedtakType.ALDERSJUSTERING,
                opprettetAv = "ABCDEFG",
                opprettetAvNavn = "Saksbehandler1",
                vedtakTidspunkt = LocalDateTime.now(),
                enhetId = "ABCD",
                utsattTilDato = LocalDate.now(),
                grunnlagListe = emptyList(),
                stonadsendringListe = listOf(
                    OpprettStonadsendringRequestDto(
                        type = StonadType.BIDRAG, sakId = "B", skyldnerId = "C", kravhaverId = "D", mottakerId = "E",
                        indeksreguleringAar = "2024", innkreving = Innkreving.JA, endring = true,
                        omgjorVedtakId = null, eksternReferanse = null,
                        listOf(
                            OpprettVedtakPeriodeRequestDto(
                                fomDato = LocalDate.now(),
                                tilDato = LocalDate.now(),
                                belop = BigDecimal.ONE,
                                valutakode = "NOK",
                                resultatkode = "A",
                                delytelseId = null,
                                listOf("A")
                            )
                        )
                    )
                ),
                engangsbelopListe = listOf(
                    OpprettEngangsbelopRequestDto(
                        type = EngangsbelopType.SAERTILSKUDD, sakId = "sak01", skyldnerId = "D", kravhaverId = "E", mottakerId = "F",
                        belop = BigDecimal.ONE, valutakode = "NOK", resultatkode = "A", innkreving = Innkreving.JA,
                        endring = true, omgjorVedtakId = null, referanse = "referanse1", delytelseId = null, eksternReferanse = null,
                        listOf("A")
                    )
                ),
                behandlingsreferanseListe = null
            ),
            1,
            LocalDateTime.now()
        )

        verify(vedtakEventProducerMock).publish(anyOrNull())
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal opprette en hendelse med skyldner-id`() {
        CorrelationId.existing("test")
        hendelserService.opprettHendelse(
            OpprettVedtakRequestDto(
                kilde = VedtakKilde.MANUELT,
                type = VedtakType.ALDERSJUSTERING,
                opprettetAv = "ABCDEFG",
                opprettetAvNavn = "Saksbehandler1",
                vedtakTidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
                enhetId = "ABCD",
                utsattTilDato = LocalDate.now(),
                grunnlagListe = emptyList(),
                stonadsendringListe = listOf(
                    OpprettStonadsendringRequestDto(
                        type = StonadType.BIDRAG, sakId = "B", skyldnerId = "C", kravhaverId = "D",
                        mottakerId = "E", indeksreguleringAar = "2024", innkreving = Innkreving.JA,
                        endring = true, omgjorVedtakId = null, eksternReferanse = null,
                        listOf(
                            OpprettVedtakPeriodeRequestDto(
                                LocalDate.now(),
                                LocalDate.now(),
                                BigDecimal.ONE,
                                "NOK",
                                "A",
                                "delytelseId1",
                                listOf("A")
                            )
                        )
                    )
                ),
                engangsbelopListe = emptyList(),
                behandlingsreferanseListe = listOf(
                    OpprettBehandlingsreferanseRequestDto(
                        kilde = BehandlingsrefKilde.BISYS_SOKNAD,
                        referanse = "referanse1"
                    )
                )
            ),
            vedtakId = 1,
            opprettetTidspunkt = LocalDateTime.parse("2021-07-06T09:31:25.007971200")
        )

        verify(vedtakEventProducerMock).publish(
            VedtakHendelse(
                kilde = VedtakKilde.MANUELT,
                type = VedtakType.ALDERSJUSTERING,
                id = 1,
                vedtakTidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
                enhetId = "ABCD",
                utsattTilDato = LocalDate.now(),
                opprettetAv = "ABCDEFG",
                opprettetAvNavn = "Saksbehandler1",
                opprettetTidspunkt = LocalDateTime.parse("2021-07-06T09:31:25.007971200"),
                listOf(
                    Stonadsendring(
                        type = StonadType.BIDRAG,
                        sakId = "B",
                        skyldnerId = "C",
                        kravhaverId = "D",
                        mottakerId = "E",
                        indeksreguleringAar = "2024",
                        innkreving = Innkreving.JA,
                        endring = true,
                        omgjorVedtakId = null,
                        eksternReferanse = null,
                        listOf(
                            Periode(
                                fomDato = LocalDate.now(),
                                tilDato = LocalDate.now(),
                                belop = BigDecimal.valueOf(1),
                                valutakode = "NOK",
                                resultatkode = "A",
                                delytelseId = "delytelseId1"
                            )
                        )
                    )
                ),
                engangsbelopListe = emptyList(),
                behandlingsreferanseListe = listOf(
                    Behandlingsreferanse(
                        kilde = BehandlingsrefKilde.BISYS_SOKNAD.toString(),
                        referanse = "referanse1"
                    )
                ),
                sporingsdata = Sporingsdata("test")
            )
        )
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal opprette hendelse ved engangsbeløp SAERTILSKUDD`() {
        hendelserService.opprettHendelse(
            OpprettVedtakRequestDto(
                kilde = VedtakKilde.MANUELT,
                type = VedtakType.ALDERSJUSTERING,
                opprettetAv = "ABCDEFG",
                opprettetAvNavn = "Saksbehandler1",
                vedtakTidspunkt = LocalDateTime.now(),
                enhetId = "ABCD",
                utsattTilDato = LocalDate.now(),
                grunnlagListe = emptyList(),
                stonadsendringListe = emptyList(),
                engangsbelopListe = listOf(
                    OpprettEngangsbelopRequestDto(
                        type = EngangsbelopType.SAERTILSKUDD,
                        sakId = "SAK-101",
                        skyldnerId = "skyldner",
                        kravhaverId = "kravhaver",
                        mottakerId = "mottaker",
                        belop = BigDecimal.ONE,
                        resultatkode = "all is well",
                        valutakode = "Nok",
                        innkreving = Innkreving.JA,
                        endring = true,
                        omgjorVedtakId = null,
                        referanse = "referanse1",
                        delytelseId = null,
                        eksternReferanse = null,
                        grunnlagReferanseListe = listOf("A")
                    )
                ),
                behandlingsreferanseListe = emptyList()
            ),
            vedtakId = 1,
            opprettetTidspunkt = LocalDateTime.now()
        )
        verify(vedtakEventProducerMock).publish(anyOrNull())
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `opprettet hendelse skal ha innhold fra engangsbelopListe`() {
        CorrelationId.existing("test")
        hendelserService.opprettHendelse(
            OpprettVedtakRequestDto(
                kilde = VedtakKilde.MANUELT,
                type = VedtakType.ALDERSJUSTERING,
                opprettetAv = "ABCDEFG",
                opprettetAvNavn = "Saksbehandler1",
                vedtakTidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
                enhetId = "ABCD",
                utsattTilDato = LocalDate.now(),
                grunnlagListe = emptyList(),
                stonadsendringListe = emptyList(),
                engangsbelopListe = listOf(
                    OpprettEngangsbelopRequestDto(
                        type = EngangsbelopType.SAERTILSKUDD,
                        sakId = "SAK-101",
                        skyldnerId = "skyldner",
                        kravhaverId = "kravhaver",
                        mottakerId = "mottaker",
                        belop = BigDecimal.valueOf(2),
                        resultatkode = "all is well",
                        valutakode = "Nok",
                        innkreving = Innkreving.JA,
                        endring = true,
                        omgjorVedtakId = null,
                        referanse = "referanse1",
                        delytelseId = null,
                        eksternReferanse = null,
                        grunnlagReferanseListe = listOf("A")
                    )
                ),
                behandlingsreferanseListe = emptyList()
            ),
            vedtakId = 1,
            opprettetTidspunkt = LocalDateTime.parse("2021-07-06T09:31:25.007971200")
        )
        verify(vedtakEventProducerMock).publish(
            VedtakHendelse(
                kilde = VedtakKilde.MANUELT,
                type = VedtakType.ALDERSJUSTERING,
                id = 1,
                vedtakTidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
                enhetId = "ABCD",
                utsattTilDato = LocalDate.now(),
                opprettetAv = "ABCDEFG",
                opprettetAvNavn = "Saksbehandler1",
                opprettetTidspunkt = LocalDateTime.parse("2021-07-06T09:31:25.007971200"),
                stonadsendringListe = emptyList(),
                engangsbelopListe =
                listOf(
                    Engangsbelop(
                        type = EngangsbelopType.SAERTILSKUDD,
                        sakId = "SAK-101",
                        skyldnerId = "skyldner",
                        kravhaverId = "kravhaver",
                        mottakerId = "mottaker",
                        belop = BigDecimal.valueOf(2),
                        resultatkode = "all is well",
                        valutakode = "Nok",
                        innkreving = Innkreving.JA,
                        endring = true,
                        omgjorVedtakId = null,
                        referanse = "referanse1",
                        delytelseId = null,
                        eksternReferanse = null
                    )
                ),
                behandlingsreferanseListe = emptyList(),
                sporingsdata = Sporingsdata("test")
            )
        )
    }
}
