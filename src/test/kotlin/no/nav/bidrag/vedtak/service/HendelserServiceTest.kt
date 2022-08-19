package no.nav.bidrag.vedtak.service

import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettEngangsbelopRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettStonadsendringRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettVedtakPeriodeRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettVedtakRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakHendelse
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakHendelsePeriode
import no.nav.bidrag.behandling.felles.enums.EngangsbelopType
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.vedtak.BidragVedtakTest
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer
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
      OpprettVedtakRequestDto(
        vedtakType = VedtakType.MANUELT,
        opprettetAv = "ABCDEFG",
        vedtakDato = LocalDate.now(),
        enhetId = "ABCD",
        grunnlagListe = emptyList(),
        stonadsendringListe = null,
        engangsbelopListe = listOf(
          OpprettEngangsbelopRequestDto(
            1, EngangsbelopType.SAERTILSKUDD, "sak01","D", "E", "F",
            BigDecimal.ONE, "NOK", "A",
            listOf("A")
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
      OpprettVedtakRequestDto(
        vedtakType = VedtakType.MANUELT,
        opprettetAv = "ABCDEFG",
        vedtakDato = LocalDate.now(),
        enhetId = "ABCD",
        grunnlagListe = emptyList(),
        stonadsendringListe = listOf(
          OpprettStonadsendringRequestDto(
            StonadType.BIDRAG, "B", "C", "D", "E", "F", listOf(
              OpprettVedtakPeriodeRequestDto(
                LocalDate.now(), LocalDate.now(), BigDecimal.ONE, "NOK", "A", listOf("A")
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
      OpprettVedtakRequestDto(
        vedtakType = VedtakType.MANUELT,
        opprettetAv = "ABCDEFG",
        vedtakDato = LocalDate.now(),
        enhetId = "ABCD",
        grunnlagListe = emptyList(),
        stonadsendringListe = listOf(
          OpprettStonadsendringRequestDto(
            StonadType.BIDRAG, "B", "C", "1", "E", "F", listOf(
              OpprettVedtakPeriodeRequestDto(
                LocalDate.now(), LocalDate.now(), BigDecimal.ONE, "NOK", "A", listOf("A")
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
          valutakode = "NOK", resultatkode = "A")
        ))
    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal ikke opprette hendelse ved engangsbeløp SAERTILSKUDD`() {
    hendelserService.opprettHendelse(
      OpprettVedtakRequestDto(
        vedtakType = VedtakType.MANUELT,
        opprettetAv = "ABCDEFG",
        vedtakDato = LocalDate.now(),
        enhetId = "ABCD",
        grunnlagListe = emptyList(),
        stonadsendringListe = emptyList(),
        engangsbelopListe = listOf(
          OpprettEngangsbelopRequestDto(
            endrerEngangsbelopId = 1,
            type = EngangsbelopType.SAERTILSKUDD,
            sakId = "SAK-101",
            skyldnerId = "skyldner",
            kravhaverId = "kravhaver",
            mottakerId = "mottaker",
            belop = BigDecimal.ONE,
            resultatkode = "all is well",
            valutakode = "Nok",
            grunnlagReferanseListe = listOf("A")
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
      OpprettVedtakRequestDto(
        vedtakType = VedtakType.MANUELT,
        opprettetAv = "ABCDEFG",
        vedtakDato = LocalDate.now(),
        enhetId = "ABCD",
        grunnlagListe = emptyList(),
        stonadsendringListe = listOf(
          OpprettStonadsendringRequestDto(
            StonadType.BIDRAG, "B", "C", "1", "E", "F", listOf(
              OpprettVedtakPeriodeRequestDto(
                LocalDate.now(), LocalDate.now(), BigDecimal.ONE, "NOK", "A", listOf("A")
              )
            )
          )
        ),
        engangsbelopListe = listOf(
          OpprettEngangsbelopRequestDto(
            endrerEngangsbelopId = 1,
            type = EngangsbelopType.SAERTILSKUDD,
            sakId = "SAK-101",
            skyldnerId = "skyldner",
            kravhaverId = "kravhaver",
            mottakerId = "mottaker",
            belop = BigDecimal.ONE,
            resultatkode = "all is well",
            valutakode = "Nok",
            grunnlagReferanseListe = listOf("A")
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