package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.dto.PeriodeDto
import no.nav.bidrag.vedtak.persistence.entity.Periode
import no.nav.bidrag.vedtak.persistence.repository.PeriodeRepository
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service

@Service
class PeriodePersistenceService (val periodeRepository: PeriodeRepository, val modelMapper: ModelMapper) {

  fun opprettNyPeriode(dto: PeriodeDto): PeriodeDto {
    val entity = modelMapper.map(dto, Periode::class.java)
    val periode = periodeRepository.save(entity)
    return PeriodeDto(periode.periodeId!!, periode.periodeFom, periode.periodeTom, periode.stonadId,
    periode.belop, periode.valutakode, periode.resultatkode, periode.opprettetAv, periode.opprettetTimestamp)
  }

  fun finnPeriode(id: Int): PeriodeDto {
    val periode = periodeRepository.findById(id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke periode med id %d i databasen", id)) }
    return PeriodeDto(periode.periodeId!!, periode.periodeFom, periode.periodeTom, periode.stonadId,
      periode.belop, periode.valutakode, periode.resultatkode, periode.opprettetAv, periode.opprettetTimestamp)
  }

  fun finnAllePerioderForStonad(idListe: List<Int>): List<PeriodeDto> {
    val periodeDtoListe = mutableListOf<PeriodeDto>()
    periodeRepository.findAllById(idListe)
      .forEach {periode -> periodeDtoListe.add(
        PeriodeDto(periode.periodeId!!, periode.periodeFom, periode.periodeTom, periode.stonadId,
          periode.belop, periode.valutakode, periode.resultatkode, periode.opprettetAv, periode.opprettetTimestamp)
      )}
    return periodeDtoListe
  }
}