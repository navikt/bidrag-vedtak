package no.nav.bidrag.vedtak.exception.custom

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettVedtakRequestDto
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

private val objectmapper = ObjectMapper().findAndRegisterModules()
fun OpprettVedtakRequestDto.manglerOpprettetAv(): Nothing = throw HttpClientErrorException(
    HttpStatus.BAD_REQUEST,
    "Forespørsel mangler informasjon om hvem som forsøker å opprette vedtak",
    objectmapper.writeValueAsBytes(this.copy(opprettetAv = "Opprettet av kan ikke være tom. Må være null eller satt til en verdi")),
    null,
)

fun OpprettVedtakRequestDto.duplikateReferanserEngangsbeløp(): Nothing = throw HttpClientErrorException(
    HttpStatus.BAD_REQUEST,
    "Det ligger minst to like referanser for engangsbeløp i vedtaket. Referansene må være unike innenfor et vedtak",
    objectmapper.writeValueAsBytes(this),
    null,
)

fun OpprettVedtakRequestDto.referanseTilPåklagetEngangsbeløpMangler(): Nothing = throw HttpClientErrorException(
    HttpStatus.BAD_REQUEST,
    "Det mangler referanse til påklaget engangsbeløp i vedtaket",
    objectmapper.writeValueAsBytes(this),
    null,
)

fun OpprettVedtakRequestDto.angittVedtakForOppdaterVedtaksforslagFinnesIkke(): Nothing = throw HttpClientErrorException(
    HttpStatus.BAD_REQUEST,
    "Angitt vedtak i mottatt forspørsel for å oppdatere vedtaksforslag finnes ikke",
    objectmapper.writeValueAsBytes(this),
    null,
)
