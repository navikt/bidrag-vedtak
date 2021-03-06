package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.dto.StonadsendringDto
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.dto.toStonadsendringEntity
import no.nav.bidrag.vedtak.dto.toVedtakEntity
import no.nav.bidrag.vedtak.persistence.entity.toStonadsendringDto
import no.nav.bidrag.vedtak.persistence.entity.toVedtakDto
import no.nav.bidrag.vedtak.persistence.repository.StonadsendringRepository
import no.nav.bidrag.vedtak.persistence.repository.VedtakRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PersistenceService(
  val vedtakRepository: VedtakRepository,
  val stonadsendringRepository: StonadsendringRepository,
) {

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
  fun opprettNyStonadsendring(dto: StonadsendringDto): StonadsendringDto {
    val eksisterendeVedtak = vedtakRepository.findById(dto.vedtakId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", dto.vedtakId)) }
    val nyStonadsendring = dto.toStonadsendringEntity(eksisterendeVedtak)
    val stonadsendring = stonadsendringRepository.save(nyStonadsendring)
    return stonadsendring.toStonadsendringDto()
  }

  fun finnEnStonadsendring(id: Int): StonadsendringDto {
    val stonadsendring = stonadsendringRepository.findById(id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke stønadsendring med id %d i databasen", id)) }
    return stonadsendring.toStonadsendringDto()
  }

  fun finnAlleStonadsendringer(): List<StonadsendringDto> {
    val stonadsendringDtoListe = mutableListOf<StonadsendringDto>()
    stonadsendringRepository.findAll().forEach { stonadsendringDtoListe.add(it.toStonadsendringDto()) }
    return stonadsendringDtoListe
  }
}
