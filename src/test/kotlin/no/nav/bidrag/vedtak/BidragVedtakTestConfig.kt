package no.nav.bidrag.vedtak

import com.nimbusds.jose.JOSEObjectType
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.transport.behandling.vedtak.VedtakHendelse
import no.nav.bidrag.transport.behandling.vedtak.VedtaksforslagHendelse
import no.nav.bidrag.vedtak.BidragVedtakLocal.Companion.LOCAL_PROFILE
import no.nav.bidrag.vedtak.BidragVedtakTest.Companion.TEST_PROFILE
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders

private val LOGGER = LoggerFactory.getLogger(BidragVedtakTestConfig::class.java)

@Configuration
@OpenAPIDefinition(
    info = Info(title = "bidrag-vedtak", version = "v1"),
    security = [SecurityRequirement(name = "bearer-key")],
)
@Profile(TEST_PROFILE, LOCAL_PROFILE)
class BidragVedtakTestConfig {

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @Bean
    fun securedTestRestTemplate(testRestTemplate: TestRestTemplate): HttpHeaderTestRestTemplate {
        val httpHeaderTestRestTemplate = HttpHeaderTestRestTemplate(testRestTemplate)
        httpHeaderTestRestTemplate.add(HttpHeaders.AUTHORIZATION) { generateTestToken() }
        return httpHeaderTestRestTemplate
    }

    private fun generateTestToken(): String {
        val iss = mockOAuth2Server.issuerUrl(ISSUER)
        val newIssuer = iss.newBuilder().host("localhost").build()
        val token = mockOAuth2Server.issueToken(
            ISSUER,
            "aud-localhost",
            DefaultOAuth2TokenCallback(
                ISSUER,
                "aud-localhost",
                JOSEObjectType.JWT.type,
                listOf("aud-localhost"),
                mapOf("iss" to newIssuer.toString()),
                3600,
            ),
        )
        return "Bearer " + token.serialize()
    }

    @Bean
    fun vedtakKafkaEventProducer() = TestVedtakKafkaEventProducer()
}

class TestVedtakKafkaEventProducer : VedtakKafkaEventProducer {
    override fun publishVedtak(vedtakHendelse: VedtakHendelse) {
        SECURE_LOGGER.info("Test Kafka vedtak: $vedtakHendelse")
    }
    override fun publishVedtaksforslag(vedtaksforslagHendelse: VedtaksforslagHendelse) {
        SECURE_LOGGER.info("Test Kafka vedtaksforslag: $vedtaksforslagHendelse")
    }
}
