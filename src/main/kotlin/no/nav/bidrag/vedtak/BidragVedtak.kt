package no.nav.bidrag.vedtak

import no.nav.bidrag.vedtak.BidragVedtakConfig.Companion.LIVE_PROFILE
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
@EnableJwtTokenValidation(ignore = ["org.springdoc"])
class BidragVedtak

fun main(args: Array<String>) {
    val profile = if (args.isEmpty()) LIVE_PROFILE else args[0]
    val app = SpringApplication(BidragVedtak::class.java)
    app.setAdditionalProfiles(profile)
    app.run(*args)
}
