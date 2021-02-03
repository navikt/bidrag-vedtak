package no.nav.bidrag.vedtak.security

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.vedtak.BidragVedtakLocal.Companion.TEST_PROFILE
import no.nav.security.token.support.test.jersey.TestTokenGeneratorResource
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders

@Configuration
@Profile(TEST_PROFILE)
open class HttpHeaderTestRestTemplateConfiguration {

  @Bean
  open fun securedTestRestTemplate(testRestTemplate: TestRestTemplate?): HttpHeaderTestRestTemplate? {
    val httpHeaderTestRestTemplate = HttpHeaderTestRestTemplate(testRestTemplate)
    httpHeaderTestRestTemplate.add(HttpHeaders.AUTHORIZATION) { generateTestToken() }
    return httpHeaderTestRestTemplate
  }

  private fun generateTestToken(): String {
    val testTokenGeneratorResource = TestTokenGeneratorResource()
    return "Bearer " + testTokenGeneratorResource.issueToken("localhost-idtoken")
  }
}
