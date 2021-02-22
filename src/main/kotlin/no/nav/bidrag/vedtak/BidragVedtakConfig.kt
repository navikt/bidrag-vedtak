package no.nav.bidrag.vedtak

import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.web.CorrelationIdFilter
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.modelmapper.ModelMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
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

  @Bean
  fun modelMapper(): ModelMapper {
    return ModelMapper()
  }

  companion object {
    const val LIVE_PROFILE = "live"
  }
}
