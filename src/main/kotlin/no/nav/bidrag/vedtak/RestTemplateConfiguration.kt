package no.nav.bidrag.vedtak

import no.nav.bidrag.commons.web.CorrelationIdFilter
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfiguration {

  @Bean
  @Scope("prototype")
  fun  restTemplate(): RestTemplate {
    val httpHeaderRestTemplate = HttpHeaderRestTemplate()
    httpHeaderRestTemplate.addHeaderGenerator(CorrelationIdFilter.CORRELATION_ID_HEADER) { CorrelationIdFilter.fetchCorrelationIdForThread() }
    return httpHeaderRestTemplate
  }
}
