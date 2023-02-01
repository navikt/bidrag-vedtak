package no.nav.bidrag.vedtak.service

import no.nav.bidrag.behandling.felles.dto.vedtak.Engangsbelop
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettEngangsbelopRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettStonadsendringRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettVedtakPeriodeRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettVedtakRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.Periode
import no.nav.bidrag.behandling.felles.dto.vedtak.Sporingsdata
import no.nav.bidrag.behandling.felles.dto.vedtak.Stonadsendring
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakHendelse
import no.nav.bidrag.behandling.felles.enums.EngangsbelopType
import no.nav.bidrag.behandling.felles.enums.Innkreving
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.behandling.felles.enums.VedtakKilde
import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.vedtak.BidragVedtakTest
import no.nav.bidrag.vedtak.bo.EngangsbelopBo
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
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
        kilde = VedtakKilde.MANUELT,
        type = VedtakType.ALDERSJUSTERING,
        opprettetAv = "ABCDEFG",
        vedtakTidspunkt = LocalDateTime.now(),
        enhetId = "ABCD",
        eksternReferanse = "eksternReferanse1",
        utsattTilDato = LocalDate.now(),
        grunnlagListe = emptyList(),
        stonadsendringListe = null,
        engangsbelopListe = listOf(
          OpprettEngangsbelopRequestDto(
            1, EngangsbelopType.SAERTILSKUDD, "sak01", "D", "E", "F",
            BigDecimal.ONE, "NOK", "A", "referanse1", Innkreving.JA, true,
            listOf("A")
          )
        ),
        behandlingsreferanseListe = null
      ),
      engangsbelopBoListe = arrayListOf(
        EngangsbelopBo(
          2, 1, EngangsbelopType.SAERTILSKUDD, "sak01", "D", "E", "F",
          BigDecimal.ONE, "NOK", "A", "referanse1", 1, Innkreving.JA,  true
        )
      ),
      1, LocalDateTime.now()
    )

    verify(vedtakEventProducerMock).publish(anyOrNull())
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal opprette en hendelse når kun stønadsendring er del av request`() {
    hendelserService.opprettHendelse(
      OpprettVedtakRequestDto(
        kilde = VedtakKilde.MANUELT,
        type= VedtakType.ALDERSJUSTERING,
        opprettetAv = "ABCDEFG",
        vedtakTidspunkt = LocalDateTime.now(),
        enhetId = "ABCD",
        eksternReferanse = "eksternReferanse1",
        utsattTilDato = LocalDate.now(),
        grunnlagListe = emptyList(),
        stonadsendringListe = listOf(
          OpprettStonadsendringRequestDto(
            StonadType.BIDRAG, "B", "C", "D", "E", "2024", Innkreving.JA, true,
            listOf(
              OpprettVedtakPeriodeRequestDto(
                LocalDate.now(),
                LocalDate.now(),
                BigDecimal.ONE,
                "NOK",
                "A",
                "referanse1",
                listOf("A")
              )
            )
          )
        ),
        engangsbelopListe = emptyList(),
        behandlingsreferanseListe = emptyList()
      ),
      engangsbelopBoListe = null,
      1, LocalDateTime.now()
    )

    verify(vedtakEventProducerMock).publish(anyOrNull())
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal opprette hendelse når både stonadsendring og engangsbelop er del av request`() {
    hendelserService.opprettHendelse(
      OpprettVedtakRequestDto(
        kilde = VedtakKilde.MANUELT,
        type= VedtakType.ALDERSJUSTERING,
        opprettetAv = "ABCDEFG",
        vedtakTidspunkt = LocalDateTime.now(),
        enhetId = "ABCD",
        eksternReferanse = "eksternReferanse1",
        utsattTilDato = LocalDate.now(),
        grunnlagListe = emptyList(),
        stonadsendringListe = listOf(
          OpprettStonadsendringRequestDto(
            StonadType.BIDRAG, "B", "C", "D", "E", "2024", Innkreving.JA, true,
            listOf(
              OpprettVedtakPeriodeRequestDto(
                LocalDate.now(),
                LocalDate.now(),
                BigDecimal.ONE,
                "NOK",
                "A",
                "referanse1",
                listOf("A")
              )
            )
          )
        ),
        engangsbelopListe = listOf(
          OpprettEngangsbelopRequestDto(
            1, EngangsbelopType.SAERTILSKUDD, "sak01", "D", "E", "F",
            BigDecimal.ONE, "NOK", "A", "referanse1", Innkreving.JA, true,
            listOf("A")
          )
        ),
        behandlingsreferanseListe = null
      ),
      engangsbelopBoListe = arrayListOf(
        EngangsbelopBo(
          2, 1, EngangsbelopType.SAERTILSKUDD, "sak01", "D", "E", "F",
          BigDecimal.ONE, "NOK", "A", "B", 1, Innkreving.JA, true,
        )
      ),
      1, LocalDateTime.now()
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
        type= VedtakType.ALDERSJUSTERING,
        opprettetAv = "ABCDEFG",
        vedtakTidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
        enhetId = "ABCD",
        eksternReferanse = "eksternReferanse1",
        utsattTilDato = LocalDate.now(),
        grunnlagListe = emptyList(),
        stonadsendringListe = listOf(
          OpprettStonadsendringRequestDto(
            StonadType.BIDRAG, "B", "C", "D", "E", "2024", Innkreving.JA, true,
            listOf(
              OpprettVedtakPeriodeRequestDto(
                LocalDate.now(),
                LocalDate.now(),
                BigDecimal.ONE,
                "NOK",
                "A",
                "referanse1",
                listOf("A")
              )
            )
          )
        ),
        engangsbelopListe = emptyList(),
        behandlingsreferanseListe = emptyList()
      ),
      engangsbelopBoListe = null,
      1, LocalDateTime.parse("2021-07-06T09:31:25.007971200")
    )

    verify(vedtakEventProducerMock).publish(
      VedtakHendelse(
        kilde = VedtakKilde.MANUELT,
        type= VedtakType.ALDERSJUSTERING,
        id = 1,
        vedtakTidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
        enhetId = "ABCD",
        eksternReferanse = "eksternReferanse1",
        utsattTilDato = LocalDate.now(),
        opprettetAv = "ABCDEFG",
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
            listOf(
              Periode(
                fomDato = LocalDate.now(),
                tilDato = LocalDate.now(),
                belop = BigDecimal.valueOf(1),
                valutakode = "NOK",
                resultatkode = "A",
                referanse = "referanse1"
              )
            )
          )
        ),
        engangsbelopListe = emptyList(),
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
        type= VedtakType.ALDERSJUSTERING,
        opprettetAv = "ABCDEFG",
        vedtakTidspunkt = LocalDateTime.now(),
        enhetId = "ABCD",
        eksternReferanse = "eksternReferanse1",
        utsattTilDato = LocalDate.now(),
        grunnlagListe = emptyList(),
        stonadsendringListe = emptyList(),
        engangsbelopListe = listOf(
          OpprettEngangsbelopRequestDto(
            endrerId = 1,
            type = EngangsbelopType.SAERTILSKUDD,
            sakId = "SAK-101",
            skyldnerId = "skyldner",
            kravhaverId = "kravhaver",
            mottakerId = "mottaker",
            belop = BigDecimal.ONE,
            resultatkode = "all is well",
            valutakode = "Nok",
            referanse = "referanse1",
            innkreving = Innkreving.JA,
            endring = true,
            grunnlagReferanseListe = listOf("A")
          )
        ),
        behandlingsreferanseListe = emptyList()
      ),
      engangsbelopBoListe = arrayListOf(
        EngangsbelopBo(
          2, 1, EngangsbelopType.SAERTILSKUDD, "sak01", "skyldner",
          "kravhaver", "mottaker",
          BigDecimal.ONE, "NOK", "all is well", "referanse1", 1, Innkreving.JA, true,
        )
      ), 1, LocalDateTime.now()
    )
    verify(vedtakEventProducerMock).publish(anyOrNull())
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal ikke opprette hendelse når engangsbelopBoListe = null`() {
    CorrelationId.existing("test")
    hendelserService.opprettHendelse(
      OpprettVedtakRequestDto(
        kilde = VedtakKilde.MANUELT,
        type= VedtakType.ALDERSJUSTERING,
        opprettetAv = "ABCDEFG",
        vedtakTidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
        enhetId = "ABCD",
        eksternReferanse = "eksternReferanse1",
        utsattTilDato = LocalDate.now(),
        grunnlagListe = emptyList(),
        stonadsendringListe = emptyList(),
        engangsbelopListe = listOf(
          OpprettEngangsbelopRequestDto(
            endrerId = 1,
            type = EngangsbelopType.SAERTILSKUDD,
            sakId = "SAK-101",
            skyldnerId = "skyldner",
            kravhaverId = "kravhaver",
            mottakerId = "mottaker",
            belop = BigDecimal.ONE,
            resultatkode = "all is well",
            valutakode = "Nok",
            referanse = "referanse1",
            innkreving = Innkreving.JA,
            endring = true,
            grunnlagReferanseListe = listOf("A")
          )
        ),
        behandlingsreferanseListe = emptyList()
      ),
      engangsbelopBoListe = null, 1, LocalDateTime.parse("2021-07-06T09:31:25.007971200")
    )
    verify(vedtakEventProducerMock).publish(
      VedtakHendelse(
        kilde = VedtakKilde.MANUELT,
        type= VedtakType.ALDERSJUSTERING,
        id = 1,
        vedtakTidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
        enhetId = "ABCD",
        eksternReferanse = "eksternReferanse1",
        utsattTilDato = LocalDate.now(),
        opprettetAv = "ABCDEFG",
        opprettetTidspunkt = LocalDateTime.parse("2021-07-06T09:31:25.007971200"),
        stonadsendringListe = emptyList(),
        engangsbelopListe = emptyList(),
        sporingsdata = Sporingsdata("test")
      )
    )
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `opprettet hendelse skal ha innhold fra engangsbelopBoListe, ikke engangsbelopListe, skal aldri være diff`() {
    CorrelationId.existing("test")
    hendelserService.opprettHendelse(
      OpprettVedtakRequestDto(
        kilde = VedtakKilde.MANUELT,
        type= VedtakType.ALDERSJUSTERING,
        opprettetAv = "ABCDEFG",
        vedtakTidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
        enhetId = "ABCD",
        eksternReferanse = "eksternReferanse1",
        utsattTilDato = LocalDate.now(),
        grunnlagListe = emptyList(),
        stonadsendringListe = emptyList(),
        engangsbelopListe = listOf(
          OpprettEngangsbelopRequestDto(
            endrerId = 1,
            type = EngangsbelopType.SAERTILSKUDD,
            sakId = "SAK-1x1",
            skyldnerId = "skyldnerx",
            kravhaverId = "kravhaverx",
            mottakerId = "mottakerx",
            belop = BigDecimal.valueOf(1),
            resultatkode = "all is wellx",
            valutakode = "Nokx",
            referanse = "referanse1x",
            innkreving = Innkreving.JA,
            endring = true,
            grunnlagReferanseListe = listOf("A")
          )
        ),
        behandlingsreferanseListe = emptyList()
      ),
      engangsbelopBoListe =
       arrayListOf(
        EngangsbelopBo(
          id = 2,
          lopenr = 1,
          type = EngangsbelopType.SAERTILSKUDD,
          sakId = "SAK-101",
          skyldnerId = "skyldner",
          kravhaverId = "kravhaver",
          mottakerId = "mottaker",
          belop = BigDecimal.valueOf(2),
          resultatkode = "all is well",
          valutakode = "Nok",
          referanse = "referanse1",
          endrerId = 1,
          innkreving = Innkreving.JA,
          endring = true
          )
        ) ,
      1, LocalDateTime.parse("2021-07-06T09:31:25.007971200")
    )
    verify(vedtakEventProducerMock).publish(
      VedtakHendelse(
        kilde = VedtakKilde.MANUELT,
        type= VedtakType.ALDERSJUSTERING,
        id = 1,
        vedtakTidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
        enhetId = "ABCD",
        eksternReferanse = "eksternReferanse1",
        utsattTilDato = LocalDate.now(),
        opprettetAv = "ABCDEFG",
        opprettetTidspunkt = LocalDateTime.parse("2021-07-06T09:31:25.007971200"),
        stonadsendringListe = emptyList(),
        engangsbelopListe =
          listOf(
            Engangsbelop(
              id = 2,
              type = EngangsbelopType.SAERTILSKUDD,
              sakId = "SAK-101",
              skyldnerId = "skyldner",
              kravhaverId = "kravhaver",
              mottakerId = "mottaker",
              belop = BigDecimal.valueOf(2),
              resultatkode = "all is well",
              valutakode = "Nok",
              referanse = "referanse1",
              endrerId = 1,
              innkreving = Innkreving.JA,
              endring = true
              )
          ),
        sporingsdata = Sporingsdata("test")
      )
    )
  }
}
