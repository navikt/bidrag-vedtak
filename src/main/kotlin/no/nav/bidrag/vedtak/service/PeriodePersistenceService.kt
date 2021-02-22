package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.dto.PeriodeDto
import no.nav.bidrag.vedtak.persistence.entity.Periode
import no.nav.bidrag.vedtak.persistence.repository.PeriodeRepository
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service

@Service
class PeriodePersistenceService (val periodeRepository: PeriodeRepository, val modelMapper: ModelMapper) {

  fun lagrePeriode(dto: PeriodeDto): Periode {
    val entity = modelMapper.map(dto, Periode::class.java)
    return periodeRepository.save(entity)
  }

  fun hentePeriode(id: Int): PeriodeDto {
    val periode = periodeRepository.findById(id).orElseThrow {
      //TODO Lag egen exception
      RuntimeException(
        String.format(
          "Fant ikke vedtak med id %d i databasen",
          id
        )
      )
    }
    return modelMapper.map(periode, PeriodeDto::class.java)
  }
}