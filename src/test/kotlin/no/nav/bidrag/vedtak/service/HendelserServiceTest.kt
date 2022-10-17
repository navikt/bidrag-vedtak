package no.nav.bidrag.vedtak.service

import no.nav.bidrag.behandling.felles.dto.vedtak.Engangsbelop
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettEngangsbelopRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettStonadsendringRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettVedtakPeriodeRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettVedtakRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.Periode
import no.nav.bidrag.behandling.felles.dto.vedtak.Stonadsendring
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakHendelse
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
  fun `skal opprette hendelse når kun engangsbelop er del av request`() {
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
            1, EngangsbelopType.SAERTILSKUDD, "sak01", "D", "E", "F",
            BigDecimal.ONE, "NOK", "A", "referanse1",
            listOf("A")
          )
        ),
        behandlingsreferanseListe = null), 1, LocalDateTime.now()
    )

    verify(vedtakEventProducerMock).publish(anyOrNull())
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal opprette en hendelse når kun stønadsendring er del av request`() {
    hendelserService.opprettHendelse(
      OpprettVedtakRequestDto(
        vedtakType = VedtakType.MANUELT,
        opprettetAv = "ABCDEFG",
        vedtakDato = LocalDate.now(),
        enhetId = "ABCD",
        grunnlagListe = emptyList(),
        stonadsendringListe = listOf(
          OpprettStonadsendringRequestDto(
            StonadType.BIDRAG, "B", "C", "D", "E", listOf(
              OpprettVedtakPeriodeRequestDto(
                LocalDate.now(), LocalDate.now(), BigDecimal.ONE, "NOK", "A", "referanse1", listOf("A")
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
  fun `skal opprette hendelse når både stonadsendring og engangsbelop er del av request`() {
    hendelserService.opprettHendelse(
      OpprettVedtakRequestDto(
        vedtakType = VedtakType.MANUELT,
        opprettetAv = "ABCDEFG",
        vedtakDato = LocalDate.now(),
        enhetId = "ABCD",
        grunnlagListe = emptyList(),
        stonadsendringListe = listOf(
          OpprettStonadsendringRequestDto(
            StonadType.BIDRAG, "B", "C", "D", "E", listOf(
              OpprettVedtakPeriodeRequestDto(
                LocalDate.now(), LocalDate.now(), BigDecimal.ONE, "NOK", "A", "referanse1", listOf("A")
              )
            )
          )
        ),
        engangsbelopListe = listOf(
          OpprettEngangsbelopRequestDto(
            1, EngangsbelopType.SAERTILSKUDD, "sak01", "D", "E", "F",
            BigDecimal.ONE, "NOK", "A","referanse1",
            listOf("A")
          )
        ),
        behandlingsreferanseListe = null
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
            StonadType.BIDRAG, "B", "C", "D", "E", listOf(
              OpprettVedtakPeriodeRequestDto(
                LocalDate.now(), LocalDate.now(), BigDecimal.ONE, "NOK", "A", "referanse1", listOf("A")
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
        vedtakType = VedtakType.MANUELT,
        vedtakId = 1,
        vedtakDato = LocalDate.now(),
        enhetId = "ABCD",
        opprettetAv = "ABCDEFG",
        opprettetTidspunkt = LocalDateTime.parse("2021-07-06T09:31:25.007971200"),
        listOf(
          Stonadsendring(
            stonadType = StonadType.BIDRAG,
            sakId = "B",
            skyldnerId = "C",
            kravhaverId = "D",
            mottakerId = "E",
            listOf(
              Periode(
                periodeFomDato = LocalDate.now(),
                periodeTilDato = LocalDate.now(),
                belop = BigDecimal.valueOf(1),
                valutakode = "NOK",
                resultatkode = "A",
                referanse = "referanse1"
              )
            )
          )
        ),
        engangsbelopListe = emptyList()
      )
    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal opprette hendelse ved engangsbeløp SAERTILSKUDD`() {
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
            referanse = "referanse1",
            grunnlagReferanseListe = listOf("A")
          )
        ),
        behandlingsreferanseListe = emptyList()
      ), 1, LocalDateTime.now()
    )
    verify(vedtakEventProducerMock).publish(anyOrNull())
  }
}