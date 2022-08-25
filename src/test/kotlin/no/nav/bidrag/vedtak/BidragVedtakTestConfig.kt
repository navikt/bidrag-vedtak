package no.nav.bidrag.vedtak

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakHendelse
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.vedtak.BidragVedtakLocal.Companion.LOCAL_PROFILE
import no.nav.bidrag.vedtak.BidragVedtakTest.Companion.TEST_PROFILE
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders

private val LOGGER = LoggerFactory.getLogger(BidragVedtakTestConfig::class.java)

@Configuration
@OpenAPIDefinition(
  info = Info(title = "bidrag-vedtak", version = "v1"),
  security = [SecurityRequirement(name = "bearer-key")]
)

@Profile(TEST_PROFILE, LOCAL_PROFILE)
class BidragVedtakTestConfig {

  @Autowired
  private var mockOAuth2Server: MockOAuth2Server? = null

  @Bean
  fun securedTestRestTemplate(testRestTemplate: TestRestTemplate?): HttpHeaderTestRestTemplate? {
    val httpHeaderTestRestTemplate = HttpHeaderTestRestTemplate(testRestTemplate)
    httpHeaderTestRestTemplate.add(HttpHeaders.AUTHORIZATION) { generateTestToken() }
    return httpHeaderTestRestTemplate
  }

  private fun generateTestToken(): String {
    val token = mockOAuth2Server?.issueToken(ISSUER, "aud-localhost", "aud-localhost")
    return "Bearer " + token?.serialize()
  }

  @Bean
  fun vedtakKafkaEventProducer() = TestVedtakKafkaEventProducer()
}

class TestVedtakKafkaEventProducer: VedtakKafkaEventProducer{
  override fun publish(vedtakHendelse: VedtakHendelse) {
    SECURE_LOGGER.info("Test Kafka: $vedtakHendelse")
  }
}