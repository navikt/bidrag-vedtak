package no.nav.bidrag.vedtak.util

import no.nav.bidrag.commons.util.SjekkForNyIdent
import no.nav.bidrag.domene.ident.Personident
import org.springframework.stereotype.Component

@Component
class IdentUtils {
    companion object {
        const val NAV_TSS_IDENT = "80000345435"
    }

    fun hentNyesteIdent(@SjekkForNyIdent ident: Personident): Personident = if (ident.verdi == "NAV") Personident(NAV_TSS_IDENT) else ident
}
