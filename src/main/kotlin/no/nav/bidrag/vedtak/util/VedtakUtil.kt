package no.nav.bidrag.vedtak.util

import no.nav.bidrag.transport.felles.commonObjectmapper

open class VedtakUtil {

    companion object {
        fun tilJson(json: Any): String = commonObjectmapper.writeValueAsString(json)
    }
}
