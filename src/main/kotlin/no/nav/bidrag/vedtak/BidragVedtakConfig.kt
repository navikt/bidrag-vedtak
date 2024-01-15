package no.nav.bidrag.vedtak

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.security.api.EnableSecurityConfiguration
import no.nav.bidrag.commons.service.organisasjon.EnableSaksbehandlernavnProvider
import no.nav.bidrag.commons.web.CorrelationIdFilter
import no.nav.bidrag.commons.web.DefaultCorsFilter
import no.nav.bidrag.commons.web.UserMdcFilter
import no.nav.bidrag.commons.web.config.RestOperationsAzure
import no.nav.bidrag.vedtak.hendelser.DefaultVedtakKafkaEventProducer
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention
import org.springframework.kafka.core.KafkaTemplate

const val LIVE_PROFILE = "live"
const val LOKAL_NAIS_PROFILE = "lokal-nais"

@Configuration
@EnableSaksbehandlernavnProvider
@EnableSecurityConfiguration
@OpenAPIDefinition(
    info = Info(title = "bidrag-vedtak", version = "v1"),
    security = [SecurityRequirement(name = "bearer-key")],
)
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
@SecurityScheme(
    bearerFormat = "JWT",
    name = "bearer-key",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
)
@EnableAspectJAutoProxy
@Import(CorrelationIdFilter::class, UserMdcFilter::class, DefaultCorsFilter::class, RestOperationsAzure::class)
class BidragVedtakConfig {
    @Bean
    fun timedAspect(registry: MeterRegistry): TimedAspect {
        return TimedAspect(registry)
    }

    @Bean
    @Profile(LIVE_PROFILE, LOKAL_NAIS_PROFILE)
    fun vedtakKafkaEventProducer(
        kafkaTemplate: KafkaTemplate<String?, String?>?,
        objectMapper: ObjectMapper,
        @Value("\${TOPIC_VEDTAK}") topic: String,
    ) = DefaultVedtakKafkaEventProducer(
        kafkaTemplate,
        objectMapper,
        topic,
    )

    @Bean
    fun exceptionLogger(): ExceptionLogger {
        return ExceptionLogger(BidragVedtak::class.java.simpleName)
    }

    @Bean
    fun clientRequestObservationConvention() = DefaultClientRequestObservationConvention()
}
