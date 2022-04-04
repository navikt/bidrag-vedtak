package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.bo.BehandlingsreferanseBo
import no.nav.bidrag.vedtak.bo.EngangsbelopBo
import no.nav.bidrag.vedtak.bo.EngangsbelopGrunnlagBo
import no.nav.bidrag.vedtak.bo.GrunnlagBo
import no.nav.bidrag.vedtak.bo.PeriodeBo
import no.nav.bidrag.vedtak.bo.PeriodeGrunnlagBo
import no.nav.bidrag.vedtak.bo.StonadsendringBo
import no.nav.bidrag.vedtak.bo.VedtakBo
import no.nav.bidrag.vedtak.bo.toBehandlingsreferanseEntity
import no.nav.bidrag.vedtak.bo.toEngangsbelopEntity
import no.nav.bidrag.vedtak.bo.toEngangsbelopGrunnlagEntity
import no.nav.bidrag.vedtak.bo.toGrunnlagEntity
import no.nav.bidrag.vedtak.bo.toPeriodeEntity
import no.nav.bidrag.vedtak.bo.toPeriodeGrunnlagEntity
import no.nav.bidrag.vedtak.bo.toStonadsendringEntity
import no.nav.bidrag.vedtak.bo.toVedtakEntity
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

  fun opprettVedtak(dto: VedtakBo): VedtakBo {
    val nyttVedtak = dto.toVedtakEntity()
    val vedtak = vedtakRepository.save(nyttVedtak)
    return vedtak.toVedtakDto()
  }

  fun hentVedtak(id: Int): VedtakBo {
    val vedtak = vedtakRepository.findById(id).orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", id)) }
    return vedtak.toVedtakDto()
  }

  fun opprettStonadsendring(dto: StonadsendringBo): StonadsendringBo {
    val eksisterendeVedtak = vedtakRepository.findById(dto.vedtakId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", dto.vedtakId)) }
    val nyStonadsendring = dto.toStonadsendringEntity(eksisterendeVedtak)
    val stonadsendring = stonadsendringRepository.save(nyStonadsendring)
    return stonadsendring.toStonadsendringDto()
  }

  fun hentAlleStonadsendringerForVedtak(id: Int): List<StonadsendringBo> {
    val stonadsendringBoListe = mutableListOf<StonadsendringBo>()
    stonadsendringRepository.hentAlleStonadsendringerForVedtak(id)
      .forEach {stonadsendring -> stonadsendringBoListe.add(stonadsendring.toStonadsendringDto()) }
    return stonadsendringBoListe
  }

  fun opprettPeriode(dto: PeriodeBo): PeriodeBo {
    val eksisterendeStonadsendring = stonadsendringRepository.findById(dto.stonadsendringId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke stonadsendring med id %d i databasen", dto.stonadsendringId)) }
    val nyPeriode = dto.toPeriodeEntity(eksisterendeStonadsendring)
    val periode = periodeRepository.save(nyPeriode)
    return periode.toPeriodeDto()
  }

  fun hentAllePerioderForStonadsendring(id: Int): List<PeriodeBo> {
    val periodeBoListe = mutableListOf<PeriodeBo>()
    periodeRepository.hentAllePerioderForStonadsendring(id)
      .forEach {periode -> periodeBoListe.add(periode.toPeriodeDto())}

    return periodeBoListe
  }

  fun opprettGrunnlag(dto: GrunnlagBo): GrunnlagBo {
    val eksisterendeVedtak = vedtakRepository.findById(dto.vedtakId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", dto.vedtakId)) }
    val nyttGrunnlag = dto.toGrunnlagEntity(eksisterendeVedtak)
    val grunnlag = grunnlagRepository.save(nyttGrunnlag)
    return grunnlag.toGrunnlagDto()
  }

  fun hentGrunnlag(id: Int): GrunnlagBo {
    val grunnlag = grunnlagRepository.findById(id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", id)) }
    return grunnlag.toGrunnlagDto()
  }

  fun hentAlleGrunnlagForVedtak(id: Int): List<GrunnlagBo> {
    val grunnlagBoListe = mutableListOf<GrunnlagBo>()
    grunnlagRepository.hentAlleGrunnlagForVedtak(id)
      .forEach {grunnlag -> grunnlagBoListe.add(grunnlag.toGrunnlagDto()) }
    return grunnlagBoListe
  }

  fun opprettPeriodeGrunnlag(dto: PeriodeGrunnlagBo): PeriodeGrunnlagBo {
    val eksisterendePeriode = periodeRepository.findById(dto.periodeId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke periode med id %d i databasen", dto.periodeId)) }
    val eksisterendeGrunnlag = grunnlagRepository.findById(dto.grunnlagId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", dto.grunnlagId)) }
    val nyttPeriodeGrunnlag = dto.toPeriodeGrunnlagEntity(eksisterendePeriode, eksisterendeGrunnlag)
    LOGGER.info("nyttPeriodeGrunnlag: $nyttPeriodeGrunnlag")
    val periodeGrunnlag = periodeGrunnlagRepository.save(nyttPeriodeGrunnlag)
    return periodeGrunnlag.toPeriodeGrunnlagDto()
  }

  fun hentAlleGrunnlagForPeriode(periodeId: Int): List<PeriodeGrunnlagBo> {
    val periodeGrunnlagBoListe = mutableListOf<PeriodeGrunnlagBo>()
    periodeGrunnlagRepository.hentAlleGrunnlagForPeriode(periodeId)
      .forEach {periodeGrunnlag -> periodeGrunnlagBoListe.add(periodeGrunnlag.toPeriodeGrunnlagDto()) }

    return periodeGrunnlagBoListe
  }

  fun opprettEngangsbelop(dto: EngangsbelopBo): EngangsbelopBo {
    val eksisterendeVedtak = vedtakRepository.findById(dto.vedtakId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", dto.vedtakId)) }
    val nyttEngangsbelop = dto.toEngangsbelopEntity(eksisterendeVedtak)
    val engangsbelop = engangsbelopRepository.save(nyttEngangsbelop)
    return engangsbelop.toEngangsbelopDto()
  }

  fun hentAlleEngangsbelopForVedtak(id: Int): List<EngangsbelopBo> {
    val engangsbelopBoListe = mutableListOf<EngangsbelopBo>()
    engangsbelopRepository.hentAlleEngangsbelopForVedtak(id)
      .forEach {engangsbelop -> engangsbelopBoListe.add(engangsbelop.toEngangsbelopDto()) }
    return engangsbelopBoListe
  }

  fun opprettEngangsbelopGrunnlag(dto: EngangsbelopGrunnlagBo): EngangsbelopGrunnlagBo {
    val eksisterendeEngangsbelop = engangsbelopRepository.findById(dto.engangsbelopId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke engangsbelop med id %d i databasen", dto.engangsbelopId)) }
    val eksisterendeGrunnlag = grunnlagRepository.findById(dto.grunnlagId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", dto.grunnlagId)) }
    val nyttEngangsbelopGrunnlag = dto.toEngangsbelopGrunnlagEntity(eksisterendeEngangsbelop, eksisterendeGrunnlag)
    LOGGER.info("nyttEngangsbelopGrunnlag: $nyttEngangsbelopGrunnlag")
    val engangsbelopGrunnlag = engangsbelopGrunnlagRepository.save(nyttEngangsbelopGrunnlag)
    return engangsbelopGrunnlag.toEngangsbelopGrunnlagDto()
  }

  fun hentAlleGrunnlagForEngangsbelop(engangsbelopId: Int): List<EngangsbelopGrunnlagBo> {
    val engangsbelopGrunnlagBoListe = mutableListOf<EngangsbelopGrunnlagBo>()
    engangsbelopGrunnlagRepository.hentAlleGrunnlagForEngangsbelop(engangsbelopId)
      .forEach {engangsbelopGrunnlag -> engangsbelopGrunnlagBoListe.add(engangsbelopGrunnlag.toEngangsbelopGrunnlagDto()) }

    return engangsbelopGrunnlagBoListe
  }

  fun opprettBehandlingsreferanse(dto: BehandlingsreferanseBo): BehandlingsreferanseBo {
    val eksisterendeVedtak = vedtakRepository.findById(dto.vedtakId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", dto.vedtakId)) }
    val nyBehandlingsreferanse = dto.toBehandlingsreferanseEntity(eksisterendeVedtak)
    val behandlingsreferanse = behandlingsreferanseRepository.save(nyBehandlingsreferanse)
    return behandlingsreferanse.toBehandlingsreferanseDto()
  }

  fun hentAlleBehandlingsreferanserForVedtak(id: Int): List<BehandlingsreferanseBo> {
    val behandlingsreferanseBoListe = mutableListOf<BehandlingsreferanseBo>()
    behandlingsreferanseRepository.hentAlleBehandlingsreferanserForVedtak(id)
      .forEach {behandlingsreferanse -> behandlingsreferanseBoListe.add(behandlingsreferanse.toBehandlingsreferanseDto()) }
    return behandlingsreferanseBoListe
  }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(PersistenceService::class.java)
  }
}
