package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.api.NyPeriodeRequest
import no.nav.bidrag.vedtak.api.NyStonadRequest
import no.nav.bidrag.vedtak.api.NyttVedtakRequest
import no.nav.bidrag.vedtak.dto.PeriodeDto
import no.nav.bidrag.vedtak.dto.StonadDto
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.service.PersistenceService
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
  private lateinit var stonadService: StonadService

  @Autowired
  private lateinit var vedtakService: VedtakService

  @Autowired
  private lateinit var periodeService: PeriodeService

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var periodePersistenceService: PeriodePersistenceService

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
    val nyStonadRequest = NyStonadRequest("BIDRAG", nyttVedtakOpprettet.vedtakId, "1111", "1111", "1111", "1111", "TEST", 1111)
    val nyStonadOpprettet = stonadService.opprettNyStonad(nyStonadRequest)

    // Oppretter ny periode
    val nyPeriodeRequest = NyPeriodeRequest(nyStonadOpprettet.stonadId, BigDecimal.valueOf(17), "NOK",
      "RESULTATKODE_TEST", "TEST"
    )
    val nyPeriodeOpprettet = periodeService.opprettNyPeriode(nyPeriodeRequest)

    assertAll(
      Executable { assertThat(nyPeriodeOpprettet).isNotNull() },
      Executable { assertThat(nyPeriodeOpprettet.opprettetAv).isEqualTo(nyPeriodeRequest.opprettetAv) },
      Executable { assertThat(nyPeriodeOpprettet.belop).isEqualTo(BigDecimal.valueOf(17)) },
      Executable { assertThat(nyPeriodeOpprettet.valutakode).isEqualTo("NOK") },
      Executable { assertThat(nyPeriodeOpprettet.resultatkode).isEqualTo("RESULTATKODE_TEST") },
      Executable { assertThat(nyStonadOpprettet.stonadType).isEqualTo("BIDRAG") }

    )
  }

  @Test
  fun `skal finne data for en periode`() {
    // Finner data for én periode
//    val nyPeriodeRequest = NyPeriodeRequest("Test", "3333")
    val nyttVedtakOpprettet = persistenceService.opprettNyttVedtak(VedtakDto(opprettetAv = "TEST", enhetsnummer = "1111"))

    // Oppretter ny stonad
    val nyStonadOpprettet = persistenceService.opprettNyStonad(
      StonadDto(
        stonadType = "BIDRAG",
        vedtakId = nyttVedtakOpprettet.vedtakId,
        behandlingId = "1111",
        skyldnerId = "1111",
        kravhaverId = "1111",
        mottakerId = "1111",
        opprettetAv = "TEST",
        enhetsnummer = 1111
      )
    )

    val nyPeriodeOpprettet = periodePersistenceService.opprettNyPeriode(PeriodeDto(
      belop = BigDecimal.valueOf(17),
      valutakode = "NOK",
      resultatkode = "RESULTATKODE_TEST",
      opprettetAv = "TEST"))



    assertAll(
      Executable { assertThat(nyPeriodeOpprettet).isNotNull() },
      Executable { assertThat(nyPeriodeOpprettet.opprettetAv).isEqualTo(nyPeriodeRequest.opprettetAv) },
      Executable { assertThat(nyPeriodeOpprettet.enhetsnummer).isEqualTo(nyPeriodeRequest.enhetsnummer) }

    )
  }

  }


