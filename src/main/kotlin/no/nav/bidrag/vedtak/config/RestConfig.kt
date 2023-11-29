/*
package no.nav.bidrag.vedtak.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.util.StdDateFormat
import no.nav.bidrag.commons.security.api.EnableSecurityConfiguration
import no.nav.bidrag.commons.web.config.RestOperationsAzure
import no.nav.bidrag.commons.web.interceptor.BearerTokenClientInterceptor
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.core5.http.io.SocketConfig
import org.apache.hc.core5.util.Timeout
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Scope
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
@EnableSecurityConfiguration
@Import(RestOperationsAzure::class)
class RestConfig {

    @Bean
    fun jackson2ObjectMapperBuilder(): Jackson2ObjectMapperBuilder {
        return Jackson2ObjectMapperBuilder()
            .dateFormat(StdDateFormat())
            .failOnUnknownProperties(false)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
    }

    @Bean("azureLongerTimeout")
    @Scope("prototype")
    fun restOperationsJwtBearerNoBuffer(
        restTemplateBuilder: RestTemplateBuilder,
        bearerTokenClientInterceptor: BearerTokenClientInterceptor
    ) = restTemplateBuilder
        .requestFactory { _ ->
            val sc = SocketConfig.custom().setSoTimeout(Timeout.ofMinutes(5)).build()
            val pb = PoolingHttpClientConnectionManagerBuilder.create().setDefaultSocketConfig(sc).build()
            val connectionManager = HttpClientBuilder.create().setConnectionManager(pb).build()
            HttpComponentsClientHttpRequestFactory(connectionManager)
        }
        .additionalInterceptors(bearerTokenClientInterceptor)
        .build()
}
*/
