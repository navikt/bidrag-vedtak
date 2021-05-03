package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.stonadsendring.OpprettStonadsendringRequest
import no.nav.bidrag.vedtak.api.stonadsendring.toStonadsendringDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StonadsendringService(val persistenceService: PersistenceService) {

  fun opprettStonadsendring(request: OpprettStonadsendringRequest) = persistenceService.opprettStonadsendring(request.toStonadsendringDto())

  fun hentStonadsendring(stonadsendring_id: Int) = persistenceService.hentStonadsendring(stonadsendring_id)

  fun hentAlleStonadsendringerForVedtak(vedtakId: Int) = persistenceService.hentAlleStonadsendringerForVedtak(vedtakId)
}