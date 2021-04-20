package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.KomplettVedtakResponse
import no.nav.bidrag.vedtak.dto.GrunnlagDto
import no.nav.bidrag.vedtak.dto.PeriodeDto
import no.nav.bidrag.vedtak.dto.PeriodeGrunnlagDto
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.dto.toGrunnlagEntity
import no.nav.bidrag.vedtak.dto.toPeriodeEntity
import no.nav.bidrag.vedtak.dto.toPeriodeGrunnlagEntity
import no.nav.bidrag.vedtak.dto.toStonadsendringEntity
import no.nav.bidrag.vedtak.dto.toVedtakEntity
import no.nav.bidrag.vedtak.persistence.entity.toGrunnlagDto
import no.nav.bidrag.vedtak.persistence.entity.toPeriodeDto
import no.nav.bidrag.vedtak.persistence.entity.toPeriodeGrunnlagDto
import no.nav.bidrag.vedtak.persistence.entity.toStonadsendringDto
import no.nav.bidrag.vedtak.persistence.entity.toVedtakDto
import no.nav.bidrag.vedtak.persistence.repository.GrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeRepository
import no.nav.bidrag.vedtak.persistence.repository.StonadsendringRepository
import no.nav.bidrag.vedtak.persistence.repository.VedtakRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PersistenceService(
  val vedtakRepository: VedtakRepository,
  val stonadsendringRepository: StonadsendringRepository,
  val periodeRepository: PeriodeRepository,
  val grunnlagRepository: GrunnlagRepository,
  val periodeGrunnlagRepository: PeriodeGrunnlagRepository
) {

  private val LOGGER = LoggerFactory.getLogger(PersistenceService::class.java)

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

  fun finnKomplettVedtak(vedtakId: Int): KomplettVedtakResponse {
    var respons = KomplettVedtakResponse()
    var vedtak = vedtakRepository.findById(vedtakId).orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", vedtakId)) }
    respons.vedtakId = vedtak.vedtakId
    respons.enhetId = vedtak.enhetId
    respons.saksbehandlerId = vedtak.saksbehandlerId
    respons.opprettetTimestamp = vedtak.opprettetTimestamp
/*    grunnlagRepository.hentAlleGrunnlagForVedtak(vedtakId)
      .forEach {grunnlag -> respons.grunnlagListe.add(grunnlag.toGrunnlagDto()) }*/
    respons.grunnlagListe = grunnlagRepository.hentAlleGrunnlagForVedtak(vedtak.vedtakId)
    respons.stonadsendringListe = stonadsendringRepository.hentAlleStonadsendringerForVedtak(vedtak.vedtakId)
      .forEach()
    return respons
//    return vedtak.toVedtakDto()
  }

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

  fun finnAlleStonadsendringerForVedtak(id: Int): List<StonadsendringDto> {
    val stonadsendringDtoListe = mutableListOf<StonadsendringDto>()
    stonadsendringRepository.hentAlleStonadsendringerForVedtak(id)
      .forEach {stonadsendring -> stonadsendringDtoListe.add(stonadsendring.toStonadsendringDto()) }
    return stonadsendringDtoListe
  }

  fun opprettNyPeriode(dto: PeriodeDto): PeriodeDto {
    val eksisterendeStonadsendring = stonadsendringRepository.findById(dto.stonadsendringId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke stonadsendring med id %d i databasen", dto.stonadsendringId)) }
    val nyPeriode = dto.toPeriodeEntity(eksisterendeStonadsendring)
    val periode = periodeRepository.save(nyPeriode)
    return periode.toPeriodeDto()
  }

  fun finnPeriode(id: Int): PeriodeDto {
    val periode = periodeRepository.findById(id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke periode med id %d i databasen", id)) }
    return periode.toPeriodeDto()
  }

  fun finnAllePerioderForStonadsendring(id: Int): List<PeriodeDto> {
    val periodeDtoListe = mutableListOf<PeriodeDto>()
    periodeRepository.hentAllePerioderForStonadsendring(id)
      .forEach {periode -> periodeDtoListe.add(periode.toPeriodeDto())}

    return periodeDtoListe
  }

  fun opprettNyttGrunnlag(dto: GrunnlagDto): GrunnlagDto {
    val eksisterendeVedtak = vedtakRepository.findById(dto.vedtakId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", dto.vedtakId)) }
    val nyttGrunnlag = dto.toGrunnlagEntity(eksisterendeVedtak)
    val grunnlag = grunnlagRepository.save(nyttGrunnlag)
    return grunnlag.toGrunnlagDto()
  }

  fun finnGrunnlag(id: Int): GrunnlagDto {
    val grunnlag = grunnlagRepository.findById(id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", id)) }
    return grunnlag.toGrunnlagDto()
  }

  fun finnAlleGrunnlagForVedtak(id: Int): List<GrunnlagDto> {
    val grunnlagDtoListe = mutableListOf<GrunnlagDto>()
    grunnlagRepository.hentAlleGrunnlagForVedtak(id)
      .forEach {grunnlag -> grunnlagDtoListe.add(grunnlag.toGrunnlagDto()) }
    return grunnlagDtoListe
  }

  fun opprettNyttPeriodeGrunnlag(dto: PeriodeGrunnlagDto): PeriodeGrunnlagDto {
    val eksisterendePeriode = periodeRepository.findById(dto.periodeId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke periode med id %d i databasen", dto.periodeId)) }
    val eksisterendeGrunnlag = grunnlagRepository.findById(dto.grunnlagId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", dto.grunnlagId)) }
    val nyttPeriodeGrunnlag = dto.toPeriodeGrunnlagEntity(eksisterendePeriode, eksisterendeGrunnlag)
    LOGGER.info("nyttPeriodeGrunnlag: $nyttPeriodeGrunnlag")
    val periodeGrunnlag = periodeGrunnlagRepository.save(nyttPeriodeGrunnlag)
    return periodeGrunnlag.toPeriodeGrunnlagDto()
  }

  fun finnPeriodeGrunnlag(periodeId: Int, grunnlagId: Int): PeriodeGrunnlagDto {
    val periodeGrunnlag = periodeGrunnlagRepository.hentPeriodeGrunnlag(periodeId, grunnlagId)
    return periodeGrunnlag.toPeriodeGrunnlagDto()
  }

  fun finnAlleGrunnlagForPeriode(periodeId: Int): List<PeriodeGrunnlagDto> {
    val periodeGrunnlagDtoListe = mutableListOf<PeriodeGrunnlagDto>()
    periodeGrunnlagRepository.hentAlleGrunnlagForPeriode(periodeId)
      .forEach {periodeGrunnlag -> periodeGrunnlagDtoListe.add(periodeGrunnlag.toPeriodeGrunnlagDto()) }

    return periodeGrunnlagDtoListe
  }
}
