package no.nav.bidrag.vedtak;

import static com.google.common.base.Predicates.or;
import static no.nav.bidrag.vedtak.controller.VedtakController.VEDTAK_NY;
import static no.nav.bidrag.vedtak.controller.VedtakController.VEDTAK_SOK;
import static springfox.documentation.builders.PathSelectors.regex;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerContext {

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.basePackage(BidragVedtak.class.getPackage().getName()))
        .paths(or(
            regex(VEDTAK_SOK + ".*"),
            regex(VEDTAK_NY + ".*")))
        .build();
  }
}
