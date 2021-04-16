package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.api.NyPeriodeRequest
import no.nav.bidrag.vedtak.api.NyStonadsendringRequest
import no.nav.bidrag.vedtak.api.NyttVedtakRequest
import no.nav.bidrag.vedtak.dto.PeriodeDto
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import no.nav.bidrag.vedtak.dto.VedtakDto
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
import java.time.LocalDate

@DisplayName("PeriodeServiceTest")
@ActiveProfiles(BidragVedtakLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PeriodeServiceTest {

  @Autowired
  private lateinit var periodeService: PeriodeService

  @Autowired
  private lateinit var stonadsendringService: StonadsendringService

  @Autowired
  private lateinit var vedtakService: VedtakService

  @Autowired
  private lateinit var vedtakRepository: VedtakRepository

  @Autowired
  private lateinit var stonadsendringRepository: StonadsendringRepository

  @Autowired
  private lateinit var grunnlagRepository: GrunnlagRepository

  @Autowired
  private lateinit var periodeGrunnlagRepository: PeriodeGrunnlagRepository

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    periodeGrunnlagRepository.deleteAll()
    grunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal opprette ny periode`() {

    // Oppretter nytt vedtak
    val nyttVedtakRequest = NyttVedtakRequest(saksbehandlerId = "1111", enhetId = "TEST")
    val nyttVedtakOpprettet = vedtakService.opprettNyttVedtak(nyttVedtakRequest)

    // Oppretter ny stonad
    val nyStonadsendringRequest = NyStonadsendringRequest(
      "BIDRAG", nyttVedtakOpprettet.vedtakId,
      "1111", "1111", "1111", "1111"
    )
    val nyStonadsendringOpprettet = stonadsendringService.opprettNyStonadsendring(nyStonadsendringRequest)

    // Oppretter ny periode
    val nyPeriodeRequest = NyPeriodeRequest(
      LocalDate.now(), LocalDate.now(), nyStonadsendringOpprettet.stonadsendringId,
      BigDecimal.valueOf(17), "NOK", "RESULTATKODE_TEST"
    )
    val nyPeriodeOpprettet = periodeService.opprettNyPeriode(nyPeriodeRequest)

    assertAll(
      Executable { assertThat(nyPeriodeOpprettet).isNotNull() },
      Executable { assertThat(nyPeriodeOpprettet.belop).isEqualTo(BigDecimal.valueOf(17)) },
      Executable { assertThat(nyPeriodeOpprettet.valutakode).isEqualTo("NOK") },
      Executable { assertThat(nyPeriodeOpprettet.resultatkode).isEqualTo("RESULTATKODE_TEST") },
      Executable { assertThat(nyStonadsendringOpprettet.stonadType).isEqualTo("BIDRAG") },
      Executable { assertThat(nyttVedtakOpprettet.enhetId).isEqualTo("TEST") }

    )
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal finne data for en periode`() {
    // Finner data for én periode

    // Oppretter nytt vedtak
    val nyttVedtakOpprettet = persistenceService.opprettNyttVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter ny stonadsendring
    val nyStonadsendringOpprettet = persistenceService.opprettNyStonadsendring(
      StonadsendringDto(
        stonadType = "BIDRAG", vedtakId = nyttVedtakOpprettet.vedtakId, behandlingId = "1111",
        skyldnerId = "1111", kravhaverId = "1111", mottakerId = "1111"
      ))

    // Oppretter ny periode
    val nyPeriodeOpprettet = persistenceService.opprettNyPeriode(
      PeriodeDto(
        periodeFomDato = LocalDate.now(),
        periodeTilDato = LocalDate.now(),
        stonadsendringId = nyStonadsendringOpprettet.stonadsendringId,
        belop = BigDecimal.valueOf(17.01),
        valutakode = "NOK",
        resultatkode = "RESULTATKODE_TEST"
      )
    )

    val periodeFunnet = periodeService.finnPeriode(nyPeriodeOpprettet.periodeId)

    assertAll(
      Executable { assertThat(periodeFunnet).isNotNull() },
      Executable { assertThat(periodeFunnet.belop).isEqualTo(nyPeriodeOpprettet.belop) },
      Executable { assertThat(periodeFunnet.valutakode).isEqualTo(nyPeriodeOpprettet.valutakode) },
      Executable { assertThat(periodeFunnet.resultatkode).isEqualTo(nyPeriodeOpprettet.resultatkode) }

    )
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }


  @Test
  fun `skal finne alle perioder for en stonadsendring`() {
    // Finner alle perioder

    // Oppretter nytt vedtak
    val nyttVedtakOpprettet1 = persistenceService.opprettNyttVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))
    val nyttVedtakOpprettet2 = persistenceService.opprettNyttVedtak(VedtakDto(17, saksbehandlerId = "TEST", enhetId = "9999"))

    // Oppretter ny stonadsendring
    val nyStonadsendringOpprettet1 = persistenceService.opprettNyStonadsendring(
      StonadsendringDto(
        stonadType = "BIDRAG", vedtakId = nyttVedtakOpprettet1.vedtakId, behandlingId = "1111",
        skyldnerId = "1111", kravhaverId = "1111", mottakerId = "1111"
      ))

    // Oppretter ny stonadsendring
    val nyStonadsendringOpprettet2 = persistenceService.opprettNyStonadsendring(
      StonadsendringDto(
        stonadType = "BIDRAG", vedtakId = nyttVedtakOpprettet2.vedtakId, behandlingId = "9999",
        skyldnerId = "9999", kravhaverId = "9999", mottakerId = "9999"
      ))

    // Oppretter nye perioder
    val nyPeriodeDtoListe = mutableListOf<PeriodeDto>()

    nyPeriodeDtoListe.add(
      persistenceService.opprettNyPeriode(
        PeriodeDto(
          periodeFomDato = LocalDate.now(),
          periodeTilDato = LocalDate.now(),
          stonadsendringId = nyStonadsendringOpprettet1.stonadsendringId,
          belop = BigDecimal.valueOf(17.02),
          valutakode = "NOK",
          resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER"
        )
      )
    )

    // Oppretter ny periode
    nyPeriodeDtoListe.add(
      persistenceService.opprettNyPeriode(
        PeriodeDto(
          periodeFomDato = LocalDate.now(),
          periodeTilDato = LocalDate.now(),
          stonadsendringId = nyStonadsendringOpprettet1.stonadsendringId,
          belop = BigDecimal.valueOf(2000.01),
          valutakode = "NOK",
          resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER"
        )
      )
    )

    // Oppretter ny periode som ikke skal bli funnet pga annen stonadsendringId
    nyPeriodeDtoListe.add(
      persistenceService.opprettNyPeriode(
        PeriodeDto(
          periodeFomDato = LocalDate.now(),
          periodeTilDato = LocalDate.now(),
          stonadsendringId = nyStonadsendringOpprettet2.stonadsendringId,
          belop = BigDecimal.valueOf(9999.99),
          valutakode = "NOK",
          resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER"
        )
      )
    )

    val stonadsendringId = nyStonadsendringOpprettet1.stonadsendringId
    val periodeFunnet = periodeService.finnAllePerioderForStonadsendring(stonadsendringId)

    assertAll(
      Executable { assertThat(periodeFunnet).isNotNull() },
      Executable { assertThat(periodeFunnet.allePerioderForStonadsendring.size).isEqualTo(2) },
      Executable { assertThat(periodeFunnet.allePerioderForStonadsendring[0].belop).isEqualTo(BigDecimal.valueOf(17.02)) },
      Executable { assertThat(periodeFunnet.allePerioderForStonadsendring[1].belop).isEqualTo(BigDecimal.valueOf(2000.01)) },
      Executable { assertThat(periodeFunnet.allePerioderForStonadsendring[0].resultatkode).isEqualTo(
        "RESULTATKODE_TEST_FLERE_PERIODER") },
      Executable {
      periodeFunnet.allePerioderForStonadsendring.forEachIndexed{ index, periode ->
        assertAll(
          Executable { assertThat(periode.stonadsendringId).isEqualTo(nyPeriodeDtoListe[index].stonadsendringId)},
          Executable { assertThat(periode.periodeId).isEqualTo(nyPeriodeDtoListe[index].periodeId)},
          Executable { assertThat(periode.belop).isEqualTo(nyPeriodeDtoListe[index].belop)}
        )
      }
    }
    )
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }
  }