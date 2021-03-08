package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.dto.StonadDto
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.dto.toStonadEntity
import no.nav.bidrag.vedtak.dto.toVedtakEntity
import no.nav.bidrag.vedtak.persistence.entity.toStonadDto
import no.nav.bidrag.vedtak.persistence.entity.toVedtakDto
import no.nav.bidrag.vedtak.persistence.repository.StonadRepository
import no.nav.bidrag.vedtak.persistence.repository.VedtakRepository
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PersistenceService(val vedtakRepository: VedtakRepository, val stonadRepository: StonadRepository, val modelMapper: ModelMapper) {

  @Transactional
  fun opprettNyttVedtak(dto: VedtakDto): VedtakDto {
    val nyttVedtak = dto.toVedtakEntity()
    val vedtak = vedtakRepository.save(nyttVedtak)
    return vedtak.toVedtakDto()
  }

  fun finnEttVedtak(id: Int): VedtakDto {
    val vedtak = vedtakRepository.findById(id).orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", id)) }
    return vedtak.toVedtakDto()
  }

  fun finnAlleVedtak(): List<VedtakDto> {
    val vedtakDtoListe = mutableListOf<VedtakDto>()
    vedtakRepository.findAll().forEach { vedtakDtoListe.add(it.toVedtakDto()) }
    return vedtakDtoListe
  }

  @Transactional
  fun opprettNyStonad(dto: StonadDto): StonadDto {
    val eksisterendeVedtak = vedtakRepository.findById(dto.vedtakId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", dto.vedtakId)) }
    val nyStonad = dto.toStonadEntity(eksisterendeVedtak)
    val stonad = stonadRepository.save(nyStonad)
    return stonad.toStonadDto()
  }

  fun finnEnStonad(id: Int): StonadDto {
    val stonad = stonadRepository.findById(id).orElseThrow { IllegalArgumentException(String.format("Fant ikke stønad med id %d i databasen", id)) }
    return stonad.toStonadDto()
  }

  fun finnAlleStonader(): List<StonadDto> {
    val stonadDtoListe = mutableListOf<StonadDto>()
    stonadRepository.findAll().forEach { stonadDtoListe.add(it.toStonadDto()) }
    return stonadDtoListe
  }
}
