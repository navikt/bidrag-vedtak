package no.nav.bidrag.vedtak

import com.google.common.base.Predicates.or
import no.nav.bidrag.vedtak.controller.VedtakController
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors.regex
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiKey
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class SwaggerContext {

  @Bean
  fun api(): Docket {
    return Docket(DocumentationType.SWAGGER_2)
      .select()
      .apis(RequestHandlerSelectors.basePackage(BidragVedtak::class.java.getPackage().name))
      .paths(or(
          regex(VedtakController.VEDTAK_SOK + ".*"),
          regex(VedtakController.VEDTAK_NY + ".*")))
      .build()
      .securitySchemes(listOf(apiKey()))
      .securityContexts(listOf(securityContext()))
  }

  private fun apiKey(): ApiKey {
    return ApiKey("mykey", "Authorization", "header")
  }

  private fun securityContext(): SecurityContext {
    return SecurityContext.builder()
      .securityReferences(defaultAuth())
      .forPaths(regex("/*.*"))
      .build()
  }

  private fun defaultAuth(): List<SecurityReference> {
    val authorizationScopes = Array(1) { AuthorizationScope("global", "accessEverything") }
    return listOf(SecurityReference("mykey", authorizationScopes))
  }
}
