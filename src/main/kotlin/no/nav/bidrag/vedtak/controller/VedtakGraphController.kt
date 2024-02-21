package no.nav.bidrag.vedtak.controller

import io.micrometer.core.annotation.Timed
import no.nav.bidrag.commons.util.TreeChild
import no.nav.bidrag.commons.util.toMermaid
import no.nav.bidrag.commons.util.toTree
import no.nav.bidrag.vedtak.service.VedtakService
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
@Timed
class VedtakGraphController(private val vedtakService: VedtakService) {

    @Suppress("unused")
    @PostMapping("/vedtak/mermaid/{vedtakId}")
    fun vedtakTilMermaid(@PathVariable vedtakId: Int): ResponseEntity<String> {
        val vedtak = vedtakService.hentVedtak(vedtakId)
        return ResponseEntity.ok().contentType(MediaType.valueOf("text/plain;charset=UTF-8"))
            .body(vedtak.toMermaid().joinToString(""))
    }

    @Suppress("unused")
    @PostMapping("/vedtak/graph/{vedtakId}")
    fun vedtakTilTre(@PathVariable vedtakId: Int): ResponseEntity<TreeChild> {
        val vedtak = vedtakService.hentVedtak(vedtakId)
        return ResponseEntity.ok(vedtak.toTree())
    }
}
