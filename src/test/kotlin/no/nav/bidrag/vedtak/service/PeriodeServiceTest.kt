package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.api.OppretteNyPeriodeRequest
import no.nav.bidrag.vedtak.api.OppretteNyttVedtakRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@DisplayName("PeriodeServiceTest")
@ActiveProfiles(BidragVedtakLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PeriodeServiceTest {

  @Autowired private lateinit var periodeService: PeriodeService

  @Test
  fun `skal opprette nyperiode`() {
    periodeService.opprettNyPeriode(OppretteNyPeriodeRequest("TEST", "1111"))
  }

  @Test
  fun `skal opprette ny periode dummy`() {
    periodeService.nyPeriodeDummy()
  }

  @Test
  fun `skal finne data for periode`() {
    val periodedata = periodeService.finnPeriodeDummy("1")
    assertThat(periodedata).isEqualTo("1")
  }
}
