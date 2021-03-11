package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.api.NyPeriodeRequest
import no.nav.bidrag.vedtak.api.NyStonadsendringRequest
import no.nav.bidrag.vedtak.api.NyttVedtakRequest
import no.nav.bidrag.vedtak.dto.PeriodeDto
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.persistence.repository.PeriodeRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.junit.jupiter.api.function.Executable
import java.math.BigDecimal

@DisplayName("PeriodeServiceTest")
@ActiveProfiles(BidragVedtakLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PeriodeServiceTest {

  @Autowired
  private lateinit var stonadsendringService: StonadsendringService

  @Autowired
  private lateinit var vedtakService: VedtakService

  @Autowired
  private lateinit var periodeService: PeriodeService

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    periodeRepository.deleteAll()
  }

  @Test
  fun `skal opprette ny periode`() {

    // Oppretter nytt vedtak
    val nyttVedtakRequest = NyttVedtakRequest("TEST", "1111")
    val nyttVedtakOpprettet = vedtakService.opprettNyttVedtak(nyttVedtakRequest)

    // Oppretter ny stonad
    val nyStonadsendringRequest = NyStonadsendringRequest("BIDRAG", nyttVedtakOpprettet.vedtakId,
      "1111", "1111", "1111", "1111", "TEST")
    val nyStonadsendringOpprettet = stonadsendringService.opprettNyStonadsendring(nyStonadsendringRequest)

    // Oppretter ny periode
    val nyPeriodeRequest = NyPeriodeRequest(nyStonadsendringOpprettet.stonadsendringId, BigDecimal.valueOf(17), "NOK",
      "RESULTATKODE_TEST", "TEST"
    )
    val nyPeriodeOpprettet = periodeService.opprettNyPeriode(nyPeriodeRequest)

    assertAll(
      Executable { assertThat(nyPeriodeOpprettet).isNotNull() },
      Executable { assertThat(nyPeriodeOpprettet.opprettetAv).isEqualTo(nyPeriodeRequest.opprettetAv) },
      Executable { assertThat(nyPeriodeOpprettet.belop).isEqualTo(BigDecimal.valueOf(17)) },
      Executable { assertThat(nyPeriodeOpprettet.valutakode).isEqualTo("NOK") },
      Executable { assertThat(nyPeriodeOpprettet.resultatkode).isEqualTo("RESULTATKODE_TEST") },
      Executable { assertThat(nyStonadsendringOpprettet.stonadType).isEqualTo("BIDRAG") },
      Executable { assertThat(nyttVedtakOpprettet.enhetsnummer).isEqualTo("TEST") }

    )
  }

  @Test
  fun `skal finne data for en periode`() {
    // Finner data for én periode

    // Oppretter nytt vedtak
    val nyttVedtakOpprettet = persistenceService.opprettNyttVedtak(VedtakDto(opprettetAv = "TEST", enhetsnummer = "1111"))

    // Oppretter ny stonadsendring
    val nyStonadsendringOpprettet = persistenceService.opprettNyStonadsendring(
      StonadsendringDto(stonadType = "BIDRAG", vedtakId = nyttVedtakOpprettet.vedtakId, behandlingId = "1111",
        skyldnerId = "1111", kravhaverId = "1111", mottakerId = "1111", opprettetAv = "TEST"))

    // Oppretter ny periode
    val nyPeriodeOpprettet = persistenceService.opprettNyPeriode(
      PeriodeDto(
        belop = BigDecimal.valueOf(17),
        valutakode = "NOK",
        resultatkode = "RESULTATKODE_TEST",
        opprettetAv = "TEST"
      )
    )

    val periodeFunnet = periodeService.finnPeriode(nyPeriodeOpprettet.periodeId)

    assertAll(
      Executable { assertThat(periodeFunnet).isNotNull() },
      Executable { assertThat(periodeFunnet.opprettetAv).isEqualTo(nyPeriodeOpprettet.opprettetAv) },
      Executable { assertThat(periodeFunnet.belop).isEqualTo(nyPeriodeOpprettet.belop) },
      Executable { assertThat(periodeFunnet.valutakode).isEqualTo(nyPeriodeOpprettet.valutakode) },
      Executable { assertThat(periodeFunnet.resultatkode).isEqualTo(nyPeriodeOpprettet.resultatkode) }

    )
  }


  @Test
  fun `skal finne alle perioder for en stonadsendring`() {
    // Finner alle perioder

    // Oppretter nytt vedtak
    val nyttVedtakOpprettet = persistenceService.opprettNyttVedtak(VedtakDto(opprettetAv = "TEST", enhetsnummer = "1111"))

    // Oppretter ny stonadsendring
    val nyStonadsendringOpprettet = persistenceService.opprettNyStonadsendring(
      StonadsendringDto(stonadType = "BIDRAG", vedtakId = nyttVedtakOpprettet.vedtakId, behandlingId = "1111",
        skyldnerId = "1111", kravhaverId = "1111", mottakerId = "1111", opprettetAv = "TEST"))

    // Oppretter nye perioder
    val nyPeriodeDtoListe = mutableListOf<PeriodeDto>()

    nyPeriodeDtoListe.add(
      persistenceService.opprettNyPeriode(
        PeriodeDto(
          belop = BigDecimal.valueOf(17),
          valutakode = "NOK",
          resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER",
          opprettetAv = "TEST"
        )
      )
    )

    // Oppretter ny periode
    nyPeriodeDtoListe.add(
      persistenceService.opprettNyPeriode(
        PeriodeDto(
          belop = BigDecimal.valueOf(2000),
          valutakode = "NOK",
          resultatkode = "RESULTATKODE_TEST_FLERE_PERIODER",
          opprettetAv = "TEST"
        )
      )
    )

    val stonadsendringIdListe = ArrayList<Int>(nyStonadsendringOpprettet.stonadsendringId)
    val periodeFunnet = periodeService.finnAllePerioderForStonad(stonadsendringIdListe)


    assertAll(
      Executable { assertThat(periodeFunnet).isNotNull() },
      Executable { assertThat(periodeFunnet.allePerioderForStonad.size).isEqualTo(2) },
      Executable { assertThat(periodeFunnet.allePerioderForStonad[0].belop).isEqualTo(BigDecimal.valueOf(17)) },
      Executable { assertThat(periodeFunnet.allePerioderForStonad[1].belop).isEqualTo(BigDecimal.valueOf(2000)) },
      Executable { assertThat(periodeFunnet.allePerioderForStonad[0].resultatkode).isEqualTo(
        "RESULTATKODE_TEST_FLERE_PERIODER") },
      Executable {

      periodeFunnet.allePerioderForStonad.forEachIndexed{ index, periode ->
        assertAll(
          Executable { assertThat(periode.stonadsendringId).isEqualTo(nyPeriodeDtoListe[index].stonadsendringId)},
          Executable { assertThat(periode.periodeId).isEqualTo(nyPeriodeDtoListe[index].periodeId)},
          Executable { assertThat(periode.belop).isEqualTo(nyPeriodeDtoListe[index].belop)}
        )
      }}

    )
  }

  }


