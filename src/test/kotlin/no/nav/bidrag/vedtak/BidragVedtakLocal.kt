package no.nav.bidrag.vedtak

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import no.nav.bidrag.vedtak.BidragVedtakLocal.Companion.LOCAL_PROFILE
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.test.context.ActiveProfiles


@SpringBootApplication
@EnableMockOAuth2Server
@EnableJwtTokenValidation(ignore = ["org.springdoc", "org.springframework"])
@ActiveProfiles(LOCAL_PROFILE)
@ComponentScan(excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [BidragVedtak::class, BidragVedtakTest::class])])
class BidragVedtakLocal {
  companion object {
    const val LOCAL_PROFILE = "local"
  }
}
fun main(args: Array<String>) {
  val wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort().dynamicHttpsPort()) //No-args constructor will start on port 8080, no HTTPS
  wireMockServer.start()

  val profile = if (args.isEmpty()) LOCAL_PROFILE else args[0]
  val app = SpringApplication(BidragVedtakLocal::class.java)
  app.setAdditionalProfiles(profile)
  app.run(*args)

  wireMockServer.resetAll()
  wireMockServer.stop()
}
