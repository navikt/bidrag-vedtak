/*
package no.nav.bidrag.vedtak.consumer

import no.nav.bidrag.commons.web.client.AbstractRestClient
import no.nav.bidrag.vedtak.config.CacheConfig.Companion.SAKSBEHANDLERINFO_CACHE
import no.nav.bidrag.vedtak.consumer.dto.SaksbehandlerInfoResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class BidragOrganisasjonConsumer(
    @Value("\${BIDRAG_ORGANISASJON_URL}") val url: URI,
    @Qualifier("azure") private val restTemplate: RestOperations
) : AbstractRestClient(restTemplate, "bidrag-organisasjon") {
    private fun createUri(path: String?) = UriComponentsBuilder.fromUri(url)
        .path(path ?: "").build().toUri()

    @Cacheable(SAKSBEHANDLERINFO_CACHE)
    fun hentSaksbehandlernavn(saksbehandlerIdent: String): SaksbehandlerInfoResponse? {
        return getForEntity(createUri("/saksbehandler/info/$saksbehandlerIdent"))
    }
}*/
