package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.api.OppretteNyttVedtakRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@DisplayName("VedtakServiceTest")
@ActiveProfiles(BidragVedtakLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VedtakServiceTest {

  @Autowired private lateinit var vedtakService: VedtakService

  @Test
  fun `skal opprette nytt vedtak`() {
    vedtakService.oprettNyttVedtak(OppretteNyttVedtakRequest("TEST", "1111"))
  }

  @Test
  fun `skal opprette nytt vedtak dummy`() {
    vedtakService.nyttVedtakDummy()
  }

  @Test
  fun `skal finne data for vedtak`() {
    val vedtaksdata = vedtakService.finnVedtakDummy("1")
    assertThat(vedtaksdata).isEqualTo("1")
  }
}
