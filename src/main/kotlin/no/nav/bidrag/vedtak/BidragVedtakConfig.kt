package no.nav.bidrag.vedtak

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.web.CorrelationIdFilter
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


const val LIVE_PROFILE = "live"

@Configuration
@OpenAPIDefinition(info = Info(title = "bidrag-vedtak", version = "v1"), security = [SecurityRequirement(name = "bearer-key")])
@EnableJwtTokenValidation(ignore = ["org.springframework"])
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
/*
    companion object {
        const val LIVE_PROFILE = "live"
    }*/
}