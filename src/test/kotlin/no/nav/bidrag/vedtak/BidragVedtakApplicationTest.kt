package no.nav.bidrag.vedtak

import no.nav.bidrag.vedtak.BidragVedtakTest.Companion.TEST_PROFILE
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = [BidragVedtakTest::class])
@ActiveProfiles(TEST_PROFILE)
@DisplayName("BidragVedtak")
@AutoConfigureWireMock(port = 0)
@EnableMockOAuth2Server
class BidragVedtakApplicationTest {

  @Test
  fun `skal laste spring-context`() {
  }
}
