package no.nav.bidrag.vedtak;

import no.nav.bidrag.commons.ExceptionLogger;
import no.nav.bidrag.commons.web.CorrelationIdFilter;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableJwtTokenValidation(ignore = {"springfox.documentation.swagger.web.ApiResourceController"})
public class BidragVedtakConfig {

  public static final String LIVE_PROFILE = "live";

  @Bean
  public ExceptionLogger exceptionLogger() {
    return new ExceptionLogger(BidragVedtak.class.getSimpleName());
  }

  @Bean
  public CorrelationIdFilter correlationIdFilter() {
    return new CorrelationIdFilter();
  }
}
