package no.nav.bidrag.vedtak

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class BidragVedtakTestConfig {

  @Bean
  open fun httpHeaderTestRestTemplate(): HttpHeaderTestRestTemplate {
    val testRestTemplate = TestRestTemplate(RestTemplateBuilder())
    return HttpHeaderTestRestTemplate(testRestTemplate)
  }
}