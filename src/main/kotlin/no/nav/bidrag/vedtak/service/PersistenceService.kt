package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import no.nav.bidrag.vedtak.persistence.repository.VedtakRepository
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service

@Service
class PersistenceService (val vedtakRepository: VedtakRepository, val modelMapper: ModelMapper) {

  fun lagreVedtak(dto: VedtakDto): VedtakDto {
    val entity = modelMapper.map(dto, Vedtak::class.java)
    val vedtak = vedtakRepository.save(entity)
    return VedtakDto(vedtak.vedtak_id!!, vedtak.opprettet_av, vedtak.opprettet_timestamp, vedtak.enhetsnummer)
  }

  fun henteVedtak(id: Int): VedtakDto {
    val vedtak = vedtakRepository.findById(id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", id)) }
    return VedtakDto(vedtak.vedtak_id!!, vedtak.opprettet_av, vedtak.opprettet_timestamp, vedtak.enhetsnummer)
  }
}