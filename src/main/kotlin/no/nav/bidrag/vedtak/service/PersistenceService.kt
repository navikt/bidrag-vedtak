package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.dto.BehandlingsreferanseDto
import no.nav.bidrag.vedtak.dto.EngangsbelopDto
import no.nav.bidrag.vedtak.dto.EngangsbelopGrunnlagDto
import no.nav.bidrag.vedtak.dto.GrunnlagDto
import no.nav.bidrag.vedtak.dto.PeriodeDto
import no.nav.bidrag.vedtak.dto.PeriodeGrunnlagDto
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.dto.toBehandlingsreferanseEntity
import no.nav.bidrag.vedtak.dto.toEngangsbelopEntity
import no.nav.bidrag.vedtak.dto.toEngangsbelopGrunnlagEntity
import no.nav.bidrag.vedtak.dto.toGrunnlagEntity
import no.nav.bidrag.vedtak.dto.toPeriodeEntity
import no.nav.bidrag.vedtak.dto.toPeriodeGrunnlagEntity
import no.nav.bidrag.vedtak.dto.toStonadsendringEntity
import no.nav.bidrag.vedtak.dto.toVedtakEntity
import no.nav.bidrag.vedtak.persistence.entity.toBehandlingsreferanseDto
import no.nav.bidrag.vedtak.persistence.entity.toEngangsbelopDto
import no.nav.bidrag.vedtak.persistence.entity.toEngangsbelopGrunnlagDto
import no.nav.bidrag.vedtak.persistence.entity.toGrunnlagDto
import no.nav.bidrag.vedtak.persistence.entity.toPeriodeDto
import no.nav.bidrag.vedtak.persistence.entity.toPeriodeGrunnlagDto
import no.nav.bidrag.vedtak.persistence.entity.toStonadsendringDto
import no.nav.bidrag.vedtak.persistence.entity.toVedtakDto
import no.nav.bidrag.vedtak.persistence.repository.BehandlingsreferanseRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbelopGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbelopRepository
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
  val periodeGrunnlagRepository: PeriodeGrunnlagRepository,
  val engangsbelopRepository: EngangsbelopRepository,
  val engangsbelopGrunnlagRepository: EngangsbelopGrunnlagRepository,
  val behandlingsreferanseRepository: BehandlingsreferanseRepository
) {

  fun opprettVedtak(dto: VedtakDto): VedtakDto {
    val nyttVedtak = dto.toVedtakEntity()
    val vedtak = vedtakRepository.save(nyttVedtak)
    return vedtak.toVedtakDto()
  }

  fun hentVedtak(id: Int): VedtakDto {
    val vedtak = vedtakRepository.findById(id).orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", id)) }
    return vedtak.toVedtakDto()
  }

  fun hentAlleVedtak(): List<VedtakDto> {
    val vedtakDtoListe = mutableListOf<VedtakDto>()
    vedtakRepository.findAll().forEach { vedtakDtoListe.add(it.toVedtakDto()) }
    return vedtakDtoListe
  }

  fun opprettStonadsendring(dto: StonadsendringDto): StonadsendringDto {
    val eksisterendeVedtak = vedtakRepository.findById(dto.vedtakId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", dto.vedtakId)) }
    val nyStonadsendring = dto.toStonadsendringEntity(eksisterendeVedtak)
    val stonadsendring = stonadsendringRepository.save(nyStonadsendring)
    return stonadsendring.toStonadsendringDto()
  }

  fun hentStonadsendring(id: Int): StonadsendringDto {
    val stonadsendring = stonadsendringRepository.findById(id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke stønadsendring med id %d i databasen", id)) }
    return stonadsendring.toStonadsendringDto()
  }

  fun hentAlleStonadsendringerForVedtak(id: Int): List<StonadsendringDto> {
    val stonadsendringDtoListe = mutableListOf<StonadsendringDto>()
    stonadsendringRepository.hentAlleStonadsendringerForVedtak(id)
      .forEach {stonadsendring -> stonadsendringDtoListe.add(stonadsendring.toStonadsendringDto()) }
    return stonadsendringDtoListe
  }

  fun opprettPeriode(dto: PeriodeDto): PeriodeDto {
    val eksisterendeStonadsendring = stonadsendringRepository.findById(dto.stonadsendringId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke stonadsendring med id %d i databasen", dto.stonadsendringId)) }
    val nyPeriode = dto.toPeriodeEntity(eksisterendeStonadsendring)
    val periode = periodeRepository.save(nyPeriode)
    return periode.toPeriodeDto()
  }

  fun hentPeriode(id: Int): PeriodeDto {
    val periode = periodeRepository.findById(id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke periode med id %d i databasen", id)) }
    return periode.toPeriodeDto()
  }

  fun hentAllePerioderForStonadsendring(id: Int): List<PeriodeDto> {
    val periodeDtoListe = mutableListOf<PeriodeDto>()
    periodeRepository.hentAllePerioderForStonadsendring(id)
      .forEach {periode -> periodeDtoListe.add(periode.toPeriodeDto())}

    return periodeDtoListe
  }

  fun opprettGrunnlag(dto: GrunnlagDto): GrunnlagDto {
    val eksisterendeVedtak = vedtakRepository.findById(dto.vedtakId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", dto.vedtakId)) }
    val nyttGrunnlag = dto.toGrunnlagEntity(eksisterendeVedtak)
    val grunnlag = grunnlagRepository.save(nyttGrunnlag)
    return grunnlag.toGrunnlagDto()
  }

  fun hentGrunnlag(id: Int): GrunnlagDto {
    val grunnlag = grunnlagRepository.findById(id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", id)) }
    return grunnlag.toGrunnlagDto()
  }

  fun hentAlleGrunnlagForVedtak(id: Int): List<GrunnlagDto> {
    val grunnlagDtoListe = mutableListOf<GrunnlagDto>()
    grunnlagRepository.hentAlleGrunnlagForVedtak(id)
      .forEach {grunnlag -> grunnlagDtoListe.add(grunnlag.toGrunnlagDto()) }
    return grunnlagDtoListe
  }

  fun opprettPeriodeGrunnlag(dto: PeriodeGrunnlagDto): PeriodeGrunnlagDto {
    val eksisterendePeriode = periodeRepository.findById(dto.periodeId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke periode med id %d i databasen", dto.periodeId)) }
    val eksisterendeGrunnlag = grunnlagRepository.findById(dto.grunnlagId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", dto.grunnlagId)) }
    val nyttPeriodeGrunnlag = dto.toPeriodeGrunnlagEntity(eksisterendePeriode, eksisterendeGrunnlag)
    LOGGER.info("nyttPeriodeGrunnlag: $nyttPeriodeGrunnlag")
    val periodeGrunnlag = periodeGrunnlagRepository.save(nyttPeriodeGrunnlag)
    return periodeGrunnlag.toPeriodeGrunnlagDto()
  }

  fun hentPeriodeGrunnlag(periodeId: Int, grunnlagId: Int): PeriodeGrunnlagDto {
    val periodeGrunnlag = periodeGrunnlagRepository.hentPeriodeGrunnlag(periodeId, grunnlagId)
    return periodeGrunnlag.toPeriodeGrunnlagDto()
  }

  fun hentAlleGrunnlagForPeriode(periodeId: Int): List<PeriodeGrunnlagDto> {
    val periodeGrunnlagDtoListe = mutableListOf<PeriodeGrunnlagDto>()
    periodeGrunnlagRepository.hentAlleGrunnlagForPeriode(periodeId)
      .forEach {periodeGrunnlag -> periodeGrunnlagDtoListe.add(periodeGrunnlag.toPeriodeGrunnlagDto()) }

    return periodeGrunnlagDtoListe
  }

  fun opprettEngangsbelop(dto: EngangsbelopDto): EngangsbelopDto {
    val eksisterendeVedtak = vedtakRepository.findById(dto.vedtakId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", dto.vedtakId)) }
    val nyttEngangsbelop = dto.toEngangsbelopEntity(eksisterendeVedtak)
    val engangsbelop = engangsbelopRepository.save(nyttEngangsbelop)
    return engangsbelop.toEngangsbelopDto()
  }

  fun hentEngangsbelop(id: Int): EngangsbelopDto {
    val engangsbelop = engangsbelopRepository.findById(id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke engangsbelop med id %d i databasen", id)) }
    return engangsbelop.toEngangsbelopDto()
  }

  fun hentAlleEngangsbelopForVedtak(id: Int): List<EngangsbelopDto> {
    val engangsbelopDtoListe = mutableListOf<EngangsbelopDto>()
    engangsbelopRepository.hentAlleEngangsbelopForVedtak(id)
      .forEach {engangsbelop -> engangsbelopDtoListe.add(engangsbelop.toEngangsbelopDto()) }
    return engangsbelopDtoListe
  }

  fun opprettEngangsbelopGrunnlag(dto: EngangsbelopGrunnlagDto): EngangsbelopGrunnlagDto {
    val eksisterendeEngangsbelop = engangsbelopRepository.findById(dto.engangsbelopId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke engangsbelop med id %d i databasen", dto.engangsbelopId)) }
    val eksisterendeGrunnlag = grunnlagRepository.findById(dto.grunnlagId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", dto.grunnlagId)) }
    val nyttEngangsbelopGrunnlag = dto.toEngangsbelopGrunnlagEntity(eksisterendeEngangsbelop, eksisterendeGrunnlag)
    LOGGER.info("nyttEngangsbelopGrunnlag: $nyttEngangsbelopGrunnlag")
    val engangsbelopGrunnlag = engangsbelopGrunnlagRepository.save(nyttEngangsbelopGrunnlag)
    return engangsbelopGrunnlag.toEngangsbelopGrunnlagDto()
  }

  fun hentEngangsbelopGrunnlag(engangsbelopId: Int, grunnlagId: Int): EngangsbelopGrunnlagDto {
    val engangsbelopGrunnlag = engangsbelopGrunnlagRepository.hentEngangsbelopGrunnlag(engangsbelopId, grunnlagId)
    return engangsbelopGrunnlag.toEngangsbelopGrunnlagDto()
  }

  fun hentAlleGrunnlagForEngangsbelop(engangsbelopId: Int): List<EngangsbelopGrunnlagDto> {
    val engangsbelopGrunnlagDtoListe = mutableListOf<EngangsbelopGrunnlagDto>()
    engangsbelopGrunnlagRepository.hentAlleGrunnlagForEngangsbelop(engangsbelopId)
      .forEach {engangsbelopGrunnlag -> engangsbelopGrunnlagDtoListe.add(engangsbelopGrunnlag.toEngangsbelopGrunnlagDto()) }

    return engangsbelopGrunnlagDtoListe
  }

  fun opprettBehandlingsreferanse(dto: BehandlingsreferanseDto): BehandlingsreferanseDto {
    val eksisterendeVedtak = vedtakRepository.findById(dto.vedtakId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", dto.vedtakId)) }
    val nyBehandlingsreferanse = dto.toBehandlingsreferanseEntity(eksisterendeVedtak)
    val behandlingsreferanse = behandlingsreferanseRepository.save(nyBehandlingsreferanse)
    return behandlingsreferanse.toBehandlingsreferanseDto()
  }

  fun hentAlleBehandlingsreferanserForVedtak(id: Int): List<BehandlingsreferanseDto> {
    val behandlingsreferanseDtoListe = mutableListOf<BehandlingsreferanseDto>()
    behandlingsreferanseRepository.hentAlleBehandlingsreferanserForVedtak(id)
      .forEach {behandlingsreferanse -> behandlingsreferanseDtoListe.add(behandlingsreferanse.toBehandlingsreferanseDto()) }
    return behandlingsreferanseDtoListe
  }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(PersistenceService::class.java)
  }
}
