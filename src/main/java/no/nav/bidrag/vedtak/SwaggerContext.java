package no.nav.bidrag.vedtak;

import static com.google.common.base.Predicates.or;
import static no.nav.bidrag.vedtak.controller.VedtakController.VEDTAK_NY;
import static no.nav.bidrag.vedtak.controller.VedtakController.VEDTAK_SOK;
import static springfox.documentation.builders.PathSelectors.regex;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
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
        .build()
        .securitySchemes(List.of(apiKey()))
        .securityContexts(List.of(securityContext()));
  }

  private ApiKey apiKey() {
    return new ApiKey("mykey", "Authorization", "header");
  }

  private SecurityContext securityContext() {
    return SecurityContext.builder()
        .securityReferences(defaultAuth())
        .forPaths(PathSelectors.regex("/*.*"))
        .build();
  }

  private List<SecurityReference> defaultAuth() {
    AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
    AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
    authorizationScopes[0] = authorizationScope;

    return List.of(new SecurityReference("mykey", authorizationScopes));
  }
}
