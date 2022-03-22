package no.nav.bidrag.vedtak.service

import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.vedtak.BidragVedtakTest
import no.nav.bidrag.vedtak.api.engangsbelop.OpprettEngangsbelopRequest
import no.nav.bidrag.vedtak.api.grunnlag.OpprettGrunnlagReferanseRequest
import no.nav.bidrag.vedtak.api.periode.OpprettPeriodeRequest
import no.nav.bidrag.vedtak.api.stonadsendring.OpprettStonadsendringRequest
import no.nav.bidrag.vedtak.api.vedtak.OpprettVedtakRequest
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer
import no.nav.bidrag.vedtak.model.VedtakHendelse
import no.nav.bidrag.vedtak.model.VedtakHendelsePeriode
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.never
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
@SpringBootTest(classes = [BidragVedtakTest::class])
@EnableMockOAuth2Server
class HendelserServiceTest {

  @Autowired
  private lateinit var hendelserService: HendelserService

  @MockBean
  private lateinit var vedtakEventProducerMock: VedtakKafkaEventProducer

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal ikke opprette hendelser når ingen stønadsendringer er del av request`() {
    hendelserService.opprettHendelse(
      OpprettVedtakRequest(
        vedtakType = VedtakType.MANUELT,
        opprettetAv = "ABCDEFG",
        vedtakDato = LocalDate.now(),
        enhetId = "ABCD",
        grunnlagListe = emptyList(),
        stonadsendringListe = null,
        engangsbelopListe = listOf(
          OpprettEngangsbelopRequest(
            1, 1, 1, "C", "D", "E", "F",
            BigDecimal.ONE, "NOK", "A",
            listOf(OpprettGrunnlagReferanseRequest("A"))
          )
        ),
        behandlingsreferanseListe = null), 1, LocalDateTime.now()
    )

    verify(vedtakEventProducerMock, never()).publish(anyOrNull())
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal opprette en hendelse når en stønadsendring er del av request`() {
    hendelserService.opprettHendelse(
      OpprettVedtakRequest(
        vedtakType = VedtakType.MANUELT,
        opprettetAv = "ABCDEFG",
        vedtakDato = LocalDate.now(),
        enhetId = "ABCD",
        grunnlagListe = emptyList(),
        stonadsendringListe = listOf(
          OpprettStonadsendringRequest(
            StonadType.BIDRAG, 1, "B", "C", "D", "E", "F",
            listOf(
              OpprettPeriodeRequest(
                LocalDate.now(), LocalDate.now(), 1, BigDecimal.ONE, "NOK", "A",
                listOf(OpprettGrunnlagReferanseRequest("A"))
              )
            )
          )
        ),
        engangsbelopListe = emptyList(),
        behandlingsreferanseListe = emptyList()
      ), 1, LocalDateTime.now()
    )

    verify(vedtakEventProducerMock).publish(anyOrNull())
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal opprette en hendelse med skyldner-id`() {
    hendelserService.opprettHendelse(
      OpprettVedtakRequest(
        vedtakType = VedtakType.MANUELT,
        opprettetAv = "ABCDEFG",
        vedtakDato = LocalDate.now(),
        enhetId = "ABCD",
        grunnlagListe = emptyList(),
        stonadsendringListe = listOf(
          OpprettStonadsendringRequest(
            StonadType.BIDRAG, 1, "B", "C", "1", "E", "F",
            listOf(
              OpprettPeriodeRequest(
                LocalDate.now(), LocalDate.now(), 1, BigDecimal.ONE, "NOK", "A",
                listOf(OpprettGrunnlagReferanseRequest("A"))
              )
            )
          )
        ),
        engangsbelopListe = emptyList(),
        behandlingsreferanseListe = emptyList()
      ), 1, LocalDateTime.parse("2021-07-06T09:31:25.007971200")
    )

    verify(vedtakEventProducerMock).publish(
      VedtakHendelse(
        vedtakId = 1, vedtakType = VedtakType.MANUELT, stonadType = StonadType.BIDRAG, sakId = "B", skyldnerId = "1", kravhaverId = "E", mottakerId = "F",
        opprettetAv = "ABCDEFG", opprettetTimestamp = LocalDateTime.parse("2021-07-06T09:31:25.007971200"),
        listOf(VedtakHendelsePeriode(periodeFom = LocalDate.now(), periodeTil = LocalDate.now(), belop = BigDecimal.valueOf(1),
          valutakode = "NOK", resultatkode = "A"))))
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal ikke opprette hendelse ved engangsbeløp SAERTILSKUDD`() {
    hendelserService.opprettHendelse(
      OpprettVedtakRequest(
        vedtakType = VedtakType.MANUELT,
        opprettetAv = "ABCDEFG",
        vedtakDato = LocalDate.now(),
        enhetId = "ABCD",
        grunnlagListe = emptyList(),
        stonadsendringListe = emptyList(),
        engangsbelopListe = listOf(
          OpprettEngangsbelopRequest(
            1, 1, 1, "C", "D", "E", "F",
            BigDecimal.ONE, "NOK", "A",
            listOf(OpprettGrunnlagReferanseRequest("A"))
          )
        ),
        behandlingsreferanseListe = emptyList()
      ), 1, LocalDateTime.now()
    )
    verify(vedtakEventProducerMock, never()).publish(anyOrNull())
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal kun opprette hendelse ved stønadsendring og ikke for engangsbeløp`() {
    hendelserService.opprettHendelse(
      OpprettVedtakRequest(
        vedtakType = VedtakType.MANUELT,
        opprettetAv = "ABCDEFG",
        vedtakDato = LocalDate.now(),
        enhetId = "ABCD",
        grunnlagListe = emptyList(),
        stonadsendringListe = listOf(
          OpprettStonadsendringRequest(
            StonadType.BIDRAG, 1, "B", "C", "1", "E", "F",
            listOf(
              OpprettPeriodeRequest(
                LocalDate.now(), LocalDate.now(), 1, BigDecimal.ONE, "NOK", "A",
                listOf(OpprettGrunnlagReferanseRequest("A"))
              )
            )
          )
        ),
        engangsbelopListe = listOf(
          OpprettEngangsbelopRequest(
            1, 1, 1, "C", "D", "E", "F",
            BigDecimal.ONE, "NOK", "A",
            listOf(OpprettGrunnlagReferanseRequest("A"))
          )
        ),
        behandlingsreferanseListe = emptyList()
      ), 1, LocalDateTime.parse("2021-07-06T09:31:25.007971200")
    )
    verify(vedtakEventProducerMock).publish(
      VedtakHendelse(
        vedtakId = 1, vedtakType = VedtakType.MANUELT, stonadType = StonadType.BIDRAG, sakId = "B", skyldnerId = "1", kravhaverId = "E", mottakerId = "F",
        opprettetAv = "ABCDEFG", opprettetTimestamp = LocalDateTime.parse("2021-07-06T09:31:25.007971200"),
        listOf(VedtakHendelsePeriode(periodeFom = LocalDate.now(), periodeTil = LocalDate.now(), belop = BigDecimal.valueOf(1),
        valutakode = "NOK", resultatkode = "A"))))
  }

}