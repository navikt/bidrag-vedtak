package no.nav.bidrag.vedtak

import no.nav.bidrag.vedtak.BidragVedtakLocal.Companion.TEST_PROFILE
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = [BidragVedtakLocal::class])
@ActiveProfiles(TEST_PROFILE)
@DisplayName("BidragVedtak")
class BidragVedtakApplicationTest {

  @Test
  @DisplayName("skal laste spring-context")
  fun contextLoads() {
  }
}
