package no.nav.bidrag.vedtak;

import no.nav.bidrag.commons.ExceptionLogger;
import no.nav.bidrag.commons.web.CorrelationIdFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BidragVedtakConfig {

  @Bean
  public ExceptionLogger exceptionLogger() {
    return new ExceptionLogger(BidragVedtak.class.getSimpleName());
  }

  @Bean
  public CorrelationIdFilter correlationIdFilter() {
    return new CorrelationIdFilter();
  }

}
