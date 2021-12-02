package no.nav.bidrag.vedtak

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.vedtak.BidragVedtakLocal.Companion.TEST_PROFILE
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer
import no.nav.bidrag.vedtak.model.VedtakHendelse
import no.nav.security.token.support.test.jersey.TestTokenGeneratorResource
import org.slf4j.LoggerFactory
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders

private val LOGGER = LoggerFactory.getLogger(BidragVedtakTestConfig::class.java)

@Configuration
@Profile(TEST_PROFILE)
class BidragVedtakTestConfig {

  @Bean
  fun securedTestRestTemplate(testRestTemplate: TestRestTemplate?): HttpHeaderTestRestTemplate? {
    val httpHeaderTestRestTemplate = HttpHeaderTestRestTemplate(testRestTemplate)
    httpHeaderTestRestTemplate.add(HttpHeaders.AUTHORIZATION) { generateTestToken() }
    return httpHeaderTestRestTemplate
  }

  private fun generateTestToken(): String {
    val testTokenGeneratorResource = TestTokenGeneratorResource()
    return "Bearer " + testTokenGeneratorResource.issueToken("localhost-idtoken")
  }

  @Bean
  fun vedtakKafkaEventProducer() = TestVedtakKafkaEventProducer()
}

class TestVedtakKafkaEventProducer: VedtakKafkaEventProducer{
  override fun publish(vedtakHendelse: VedtakHendelse) {
    LOGGER.info("Test Kafka: $vedtakHendelse")
  }
}