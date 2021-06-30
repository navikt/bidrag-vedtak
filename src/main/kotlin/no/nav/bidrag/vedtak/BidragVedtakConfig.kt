package no.nav.bidrag.vedtak

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.web.CorrelationIdFilter
import no.nav.bidrag.tilgangskontroll.SecurityUtils
import no.nav.bidrag.vedtak.hendelser.DefaultVedtakKafkaEventProducer
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.beans.factory.annotation.Value

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate
import java.util.*


const val LIVE_PROFILE = "live"

@Configuration
@OpenAPIDefinition(
  info = Info(title = "bidrag-vedtak", version = "v1"),
  security = [SecurityRequirement(name = "bearer-key")]
)
@EnableJwtTokenValidation
@SecurityScheme(
  bearerFormat = "JWT",
  name = "bearer-key",
  scheme = "bearer",
  type = SecuritySchemeType.HTTP
)

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
  fun oidcTokenManager(tokenValidationContextHolder: TokenValidationContextHolder?): OidcTokenManager? {
    return OidcTokenManager {
      Optional.ofNullable(tokenValidationContextHolder)
        .map { obj: TokenValidationContextHolder -> obj.tokenValidationContext }
        .map { tokenValidationContext: TokenValidationContext ->
          tokenValidationContext.getJwtTokenAsOptional(ISSUER)
        }
        .map { obj: Optional<JwtToken?> -> obj.get() }
        .map { obj: JwtToken -> obj.tokenAsString }
        .orElseThrow {
          IllegalStateException(
            "Kunne ikke videresende Bearer token"
          )
        }
    }
  }

  @Bean
  @Profile(LIVE_PROFILE)
  fun vedtakKafkaEventProducer(
    kafkaTemplate: KafkaTemplate<String?, String?>?,
    objectMapper: ObjectMapper,
    @Value("\$TOPIC_VEDTAK") topic: String
  ) = DefaultVedtakKafkaEventProducer(
    kafkaTemplate, objectMapper, topic
  )

  fun interface OidcTokenManager {
    fun hentIdToken(): String?
  }
/*
    companion object {
        const val LIVE_PROFILE = "live"
    }*/
}