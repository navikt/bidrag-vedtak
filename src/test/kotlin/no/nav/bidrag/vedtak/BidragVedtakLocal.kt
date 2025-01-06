package no.nav.bidrag.vedtak

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import no.nav.bidrag.commons.service.AppContext
import no.nav.bidrag.commons.service.organisasjon.SaksbehandlerInfoResponse
import no.nav.bidrag.vedtak.BidragVedtakLocal.Companion.LOCAL_PROFILE
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.junit.Assert
import org.springframework.boot.SpringApplication
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class, ManagementWebSecurityAutoConfiguration::class])
@EnableJwtTokenValidation(ignore = ["org.springdoc", "org.springframework"])
@ActiveProfiles(LOCAL_PROFILE)
@ComponentScan(excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [BidragVedtak::class, BidragVedtakTest::class])])
class BidragVedtakLocal {
    companion object {
        const val LOCAL_PROFILE = "local"
    }
}
fun main(args: Array<String>) {
    // No-args constructor will start on port 8080, no HTTPS
    val wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort().dynamicHttpsPort())
    wireMockServer.start()

    val profile = if (args.isEmpty()) LOCAL_PROFILE else args[0]
    val app = SpringApplication(BidragVedtakLocal::class.java)
    stubHentSaksbehandler()
    app.setAdditionalProfiles(profile)
    app.run(*args)

    wireMockServer.resetAll()
    wireMockServer.stop()
}

fun stubHentSaksbehandler() {
    AppContext.getBean(WireMockServer::class.java).stubFor(
        WireMock.get(WireMock.urlMatching("/organisasjon/saksbehandler/info/(.*)")).willReturn(
            WireMock.aResponse()
                .withHeader(HttpHeaders.CONNECTION, "close")
                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .withStatus(HttpStatus.OK.value())
                .withBody(jsonToString(SaksbehandlerInfoResponse("Z99999", "Fornavn Etternavn"))),
        ),
    )
}
private fun jsonToString(data: Any): String = try {
    ObjectMapper().findAndRegisterModules().writeValueAsString(data)
} catch (e: JsonProcessingException) {
    Assert.fail(e.message)
    ""
}
