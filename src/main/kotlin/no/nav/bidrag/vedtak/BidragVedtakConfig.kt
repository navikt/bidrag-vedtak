package no.nav.bidrag.vedtak

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.web.CorrelationIdFilter
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(info = Info(title = "bidrag-vedtak", version = "v1"))
@EnableJwtTokenValidation(ignore = ["org.springframework", "springfox.documentation.swagger.web.ApiResourceController"])
class BidragVedtakConfig {

  @Bean
  fun exceptionLogger(): ExceptionLogger {
    return ExceptionLogger(BidragVedtak::class.java.simpleName)
  }

  @Bean
  fun correlationIdFilter(): CorrelationIdFilter {
    return CorrelationIdFilter()
  }

  companion object {
    const val LIVE_PROFILE = "live"
  }
}
