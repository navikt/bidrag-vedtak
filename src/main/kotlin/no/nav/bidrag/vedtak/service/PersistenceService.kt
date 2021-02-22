package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import no.nav.bidrag.vedtak.persistence.repository.VedtakRepository
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service

@Service
class PersistenceService (val vedtakRepository: VedtakRepository, val modelMapper: ModelMapper) {

  fun lagreVedtak(dto: VedtakDto): Vedtak {
    val entity = modelMapper.map(dto, Vedtak::class.java)
    return vedtakRepository.save(entity)
  }

  fun henteVedtak(id: Int): VedtakDto {
    val vedtak = vedtakRepository.findById(id).orElseThrow {
      //TODO Lag egen exception
      RuntimeException(
        String.format(
          "Fant ikke vedtak med id %d i databasen",
          id
        )
      )
    }
    return modelMapper.map(vedtak, VedtakDto::class.java)
  }
}