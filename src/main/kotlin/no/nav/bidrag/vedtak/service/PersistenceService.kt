package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.bo.BehandlingsreferanseBo
import no.nav.bidrag.vedtak.bo.EngangsbelopBo
import no.nav.bidrag.vedtak.bo.EngangsbelopGrunnlagBo
import no.nav.bidrag.vedtak.bo.GrunnlagBo
import no.nav.bidrag.vedtak.bo.PeriodeBo
import no.nav.bidrag.vedtak.bo.PeriodeGrunnlagBo
import no.nav.bidrag.vedtak.bo.StonadsendringBo
import no.nav.bidrag.vedtak.bo.toBehandlingsreferanseEntity
import no.nav.bidrag.vedtak.bo.toEngangsbelopGrunnlagEntity
import no.nav.bidrag.vedtak.bo.toPeriodeEntity
import no.nav.bidrag.vedtak.bo.toPeriodeGrunnlagEntity
import no.nav.bidrag.vedtak.persistence.entity.Engangsbelop
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import no.nav.bidrag.vedtak.persistence.entity.Periode
import no.nav.bidrag.vedtak.persistence.entity.PeriodeGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.Stonadsendring
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import no.nav.bidrag.vedtak.persistence.entity.toBehandlingsreferanseBo
import no.nav.bidrag.vedtak.persistence.entity.toEngangsbelopBo
import no.nav.bidrag.vedtak.persistence.entity.toEngangsbelopGrunnlagBo
import no.nav.bidrag.vedtak.persistence.entity.toGrunnlagEntity
import no.nav.bidrag.vedtak.persistence.entity.toPeriodeBo
import no.nav.bidrag.vedtak.persistence.entity.toPeriodeGrunnlagBo
import no.nav.bidrag.vedtak.persistence.entity.toStonadsendringBo
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

  fun opprettVedtak(vedtak: Vedtak): Vedtak {
    return vedtakRepository.save(vedtak)
  }

  fun hentVedtak(id: Int): Vedtak {
    return vedtakRepository.findById(id).orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", id)) }
  }

  fun opprettStonadsendring(stonadsendring: Stonadsendring): Stonadsendring {
    vedtakRepository.findById(stonadsendring.vedtak.vedtakId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", stonadsendring.vedtak.vedtakId)) }
    return stonadsendringRepository.save(stonadsendring)
  }

  fun hentAlleStonadsendringerForVedtak(id: Int): List<StonadsendringBo> {
    val stonadsendringBoListe = mutableListOf<StonadsendringBo>()
    stonadsendringRepository.hentAlleStonadsendringerForVedtak(id)
      .forEach {stonadsendring -> stonadsendringBoListe.add(stonadsendring.toStonadsendringBo()) }
    return stonadsendringBoListe
  }

  fun opprettPeriode(periode: Periode): Periode {
    stonadsendringRepository.findById(periode.stonadsendring.stonadsendringId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke stønadsendring med id %d i databasen", periode.stonadsendring.stonadsendringId)) }
    return periodeRepository.save(periode)
  }

  fun hentAllePerioderForStonadsendring(id: Int): List<PeriodeBo> {
    val periodeBoListe = mutableListOf<PeriodeBo>()
    periodeRepository.hentAllePerioderForStonadsendring(id)
      .forEach {periode -> periodeBoListe.add(periode.toPeriodeBo())}

    return periodeBoListe
  }

  fun opprettGrunnlag(grunnlag: Grunnlag): Int {
    vedtakRepository.findById(grunnlag.vedtak.vedtakId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", grunnlag.vedtak.vedtakId)) }
    val opprettetGrunnlag = grunnlagRepository.save(grunnlag)
    return opprettetGrunnlag.grunnlagId
  }

  fun hentGrunnlag(id: Int): GrunnlagBo {
    val grunnlag = grunnlagRepository.findById(id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", id)) }
    return grunnlag.toGrunnlagEntity()
  }

  fun hentAlleGrunnlagForVedtak(id: Int): List<GrunnlagBo> {
    val grunnlagBoListe = mutableListOf<GrunnlagBo>()
    grunnlagRepository.hentAlleGrunnlagForVedtak(id)
      .forEach {grunnlag -> grunnlagBoListe.add(grunnlag.toGrunnlagEntity()) }
    return grunnlagBoListe
  }

  fun opprettPeriodeGrunnlag(periodeId: Int, grunnlagId: Int): PeriodeGrunnlag {
    val eksisterendePeriode = periodeRepository.findById(periodeId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke periode med id %d i databasen", periodeId)) }
    val eksisterendeGrunnlag = grunnlagRepository.findById(grunnlagId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", grunnlagId)) }
    val nyttPeriodeGrunnlag = PeriodeGrunnlag(eksisterendePeriode, eksisterendeGrunnlag)
    LOGGER.info("nyttPeriodeGrunnlag: $nyttPeriodeGrunnlag")
    return periodeGrunnlagRepository.save(nyttPeriodeGrunnlag)
  }

  fun hentAlleGrunnlagForPeriode(periodeId: Int): List<PeriodeGrunnlagBo> {
    val periodeGrunnlagBoListe = mutableListOf<PeriodeGrunnlagBo>()
    periodeGrunnlagRepository.hentAlleGrunnlagForPeriode(periodeId)
      .forEach {periodeGrunnlag -> periodeGrunnlagBoListe.add(periodeGrunnlag.toPeriodeGrunnlagBo()) }

    return periodeGrunnlagBoListe
  }

  fun opprettEngangsbelop(engangsbelop: Engangsbelop): Int {
    vedtakRepository.findById(engangsbelop.vedtak.vedtakId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", engangsbelop.vedtak.vedtakId)) }
    val opprettetEngangsbelop = engangsbelopRepository.save(engangsbelop)
    return opprettetEngangsbelop.engangsbelopId
  }

  fun hentAlleEngangsbelopForVedtak(id: Int): List<EngangsbelopBo> {
    val engangsbelopBoListe = mutableListOf<EngangsbelopBo>()
    engangsbelopRepository.hentAlleEngangsbelopForVedtak(id)
      .forEach {engangsbelop -> engangsbelopBoListe.add(engangsbelop.toEngangsbelopBo()) }
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
    return engangsbelopGrunnlag.toEngangsbelopGrunnlagBo()
  }

  fun hentAlleGrunnlagForEngangsbelop(engangsbelopId: Int): List<EngangsbelopGrunnlagBo> {
    val engangsbelopGrunnlagBoListe = mutableListOf<EngangsbelopGrunnlagBo>()
    engangsbelopGrunnlagRepository.hentAlleGrunnlagForEngangsbelop(engangsbelopId)
      .forEach {engangsbelopGrunnlag -> engangsbelopGrunnlagBoListe.add(engangsbelopGrunnlag.toEngangsbelopGrunnlagBo()) }

    return engangsbelopGrunnlagBoListe
  }

  fun opprettBehandlingsreferanse(dto: BehandlingsreferanseBo): BehandlingsreferanseBo {
    val eksisterendeVedtak = vedtakRepository.findById(dto.vedtakId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", dto.vedtakId)) }
    val nyBehandlingsreferanse = dto.toBehandlingsreferanseEntity(eksisterendeVedtak)
    val behandlingsreferanse = behandlingsreferanseRepository.save(nyBehandlingsreferanse)
    return behandlingsreferanse.toBehandlingsreferanseBo()
  }

  fun hentAlleBehandlingsreferanserForVedtak(id: Int): List<BehandlingsreferanseBo> {
    val behandlingsreferanseBoListe = mutableListOf<BehandlingsreferanseBo>()
    behandlingsreferanseRepository.hentAlleBehandlingsreferanserForVedtak(id)
      .forEach {behandlingsreferanse -> behandlingsreferanseBoListe.add(behandlingsreferanse.toBehandlingsreferanseBo()) }
    return behandlingsreferanseBoListe
  }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(PersistenceService::class.java)
  }
}
