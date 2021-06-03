package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.api.engangsbelop.OpprettEngangsbelopRequest
import no.nav.bidrag.vedtak.api.vedtak.OpprettVedtakRequest
import no.nav.bidrag.vedtak.dto.EngangsbelopDto
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.persistence.repository.EngangsbelopGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbelopRepository
import no.nav.bidrag.vedtak.persistence.repository.GrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeRepository
import no.nav.bidrag.vedtak.persistence.repository.StonadsendringRepository
import no.nav.bidrag.vedtak.persistence.repository.VedtakRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal

@DisplayName("EngangsbelopServiceTest")
@ActiveProfiles(BidragVedtakLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EngangsbelopServiceTest {

  @Autowired
  private lateinit var engangsbelopService: EngangsbelopService

  @Autowired
  private lateinit var vedtakService: VedtakService

  @Autowired
  private lateinit var engangsbelopGrunnlagRepository: EngangsbelopGrunnlagRepository

  @Autowired
  private lateinit var periodeGrunnlagRepository: PeriodeGrunnlagRepository

  @Autowired
  private lateinit var grunnlagRepository: GrunnlagRepository

  @Autowired
  private lateinit var engangsbelopRepository: EngangsbelopRepository

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var stonadsendringRepository: StonadsendringRepository

  @Autowired
  private lateinit var vedtakRepository: VedtakRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    engangsbelopGrunnlagRepository.deleteAll()
    periodeGrunnlagRepository.deleteAll()
    engangsbelopRepository.deleteAll()
    grunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal opprette nytt engangsbelop`() {
    // Oppretter nytt vedtak
    val nyttVedtakRequest = OpprettVedtakRequest("TEST", "1111")
    val nyttVedtakOpprettet = vedtakService.opprettVedtak(nyttVedtakRequest)

    // Oppretter ny stønadsendring
    val nyttEngangsbelopRequest = OpprettEngangsbelopRequest(
      nyttVedtakOpprettet.vedtakId,
      1,
      null,
      "SAERBIDRAG",
      "1111",
      "1111",
      "1111",
      BigDecimal.valueOf(17.0),
      "NOK",
      "Alles gut"
    )
    val nyttEngangsbelopOpprettet = engangsbelopService.opprettEngangsbelop(nyttEngangsbelopRequest)

    assertAll(
      Executable { assertThat(nyttEngangsbelopOpprettet).isNotNull() },
      Executable { assertThat(nyttEngangsbelopOpprettet.vedtakId).isEqualTo(nyttEngangsbelopRequest.vedtakId) },
      Executable { assertThat(nyttEngangsbelopOpprettet.lopenr).isEqualTo(nyttEngangsbelopRequest.lopenr) },
      Executable { assertThat(nyttEngangsbelopOpprettet.valutakode).isEqualTo(nyttEngangsbelopRequest.valutakode) }
    )
  }

  @Test
  fun `skal hente data for en engangsbelop`() {
    // Oppretter nytt vedtak
    val nyttVedtakOpprettet = persistenceService.opprettVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter ny stønadsendring
    val nyttEngangsbelopOpprettet = persistenceService.opprettEngangsbelop(
      EngangsbelopDto(
        vedtakId = nyttVedtakOpprettet.vedtakId,
        lopenr = 1,
        endrerEngangsbelopId = null,
        type = "SAERBIDRAG",
        skyldnerId = "1111",
        kravhaverId = "1111",
        mottakerId = "1111",
        belop = BigDecimal.valueOf(999.0),
        valutakode = "NOK",
        resultatkode = "SAERBIDRAG_BEREGNET"
      )
    )

    // Henter stønadsendringen som akkurat ble opprettet
    val engangsbelopFunnet = engangsbelopService.hentEngangsbelop(nyttEngangsbelopOpprettet.engangsbelopId)

    assertAll(
      Executable { assertThat(engangsbelopFunnet).isNotNull() },
      Executable { assertThat(engangsbelopFunnet.engangsbelopId).isEqualTo(nyttEngangsbelopOpprettet.engangsbelopId) },
      Executable { assertThat(engangsbelopFunnet.vedtakId).isEqualTo(nyttEngangsbelopOpprettet.vedtakId) },
      Executable { assertThat(engangsbelopFunnet.lopenr).isEqualTo(nyttEngangsbelopOpprettet.lopenr) },
      Executable { assertThat(engangsbelopFunnet.skyldnerId).isEqualTo(nyttEngangsbelopOpprettet.skyldnerId) }
    )
    engangsbelopRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal hente alle engangsbelop for et vedtak`() {

    // Oppretter nytt vedtak
    val nyttVedtakOpprettet1 = persistenceService.opprettVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))
    val nyttVedtakOpprettet2 = persistenceService.opprettVedtak(VedtakDto(17, saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter nye engangsbelop
    val nyEngangsbelopDtoListe = mutableListOf<EngangsbelopDto>()

    nyEngangsbelopDtoListe.add(
      persistenceService.opprettEngangsbelop(
        EngangsbelopDto(
          vedtakId = nyttVedtakOpprettet1.vedtakId,
          lopenr = 1,
          endrerEngangsbelopId = null,
          type = "SAERBIDRAG",
          skyldnerId = "1111",
          kravhaverId = "1111",
          mottakerId = "1111",
          belop = BigDecimal.valueOf(999.0),
          valutakode = "NOK",
          resultatkode = "SAERBIDRAG_BEREGNET"
        )
      )
    )

    nyEngangsbelopDtoListe.add(
      persistenceService.opprettEngangsbelop(
        EngangsbelopDto(
          vedtakId = nyttVedtakOpprettet1.vedtakId,
          lopenr = 2,
          endrerEngangsbelopId = 1,
          type = "SAERBIDRAG",
          skyldnerId = "1111",
          kravhaverId = "1111",
          mottakerId = "1111",
          belop = BigDecimal.valueOf(2000.0),
          valutakode = "NOK",
          resultatkode = "SAERBIDRAG_BEREGNET"
        )
      )
    )

    // Legger til en ekstra Engangsbelop som ikke skal bli funnet pga annen vedtakId
    nyEngangsbelopDtoListe.add(
      persistenceService.opprettEngangsbelop(
        EngangsbelopDto(
          vedtakId = nyttVedtakOpprettet2.vedtakId,
          lopenr = 1,
          endrerEngangsbelopId = 1,
          type = "SAERBIDRAG",
          skyldnerId = "1111",
          kravhaverId = "1111",
          mottakerId = "1111",
          belop = BigDecimal.valueOf(555.0),
          valutakode = "NOK",
          resultatkode = "SAERBIDRAG_BEREGNET"
        )
      )
    )

    // Henter begge stønadsendringene som akkurat ble opprettet
    val vedtakId = nyttVedtakOpprettet1.vedtakId
    val engangsbelopFunnet = engangsbelopService.hentAlleEngangsbelopForVedtak(vedtakId)

    assertAll(
      Executable { assertThat(engangsbelopFunnet).isNotNull() },
      Executable { assertThat(engangsbelopFunnet.size).isEqualTo(2) },
      Executable {
        engangsbelopFunnet.forEachIndexed { index, engangsbelop ->
          assertAll(
            Executable { assertThat(engangsbelop.engangsbelopId).isEqualTo(nyEngangsbelopDtoListe[index].engangsbelopId) },
            Executable { assertThat(engangsbelop.vedtakId).isEqualTo(nyEngangsbelopDtoListe[index].vedtakId) },
            Executable { assertThat(engangsbelop.lopenr).isEqualTo(nyEngangsbelopDtoListe[index].lopenr) }
          )
        }
      }
    )
    engangsbelopRepository.deleteAll()
    vedtakRepository.deleteAll()
  }
}