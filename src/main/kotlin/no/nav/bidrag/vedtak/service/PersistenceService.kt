package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.SECURE_LOGGER
import no.nav.bidrag.vedtak.bo.EngangsbelopGrunnlagBo
import no.nav.bidrag.vedtak.bo.PeriodeGrunnlagBo
import no.nav.bidrag.vedtak.persistence.entity.Behandlingsreferanse
import no.nav.bidrag.vedtak.persistence.entity.Engangsbelop
import no.nav.bidrag.vedtak.persistence.entity.EngangsbelopGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import no.nav.bidrag.vedtak.persistence.entity.Periode
import no.nav.bidrag.vedtak.persistence.entity.PeriodeGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.Stonadsendring
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
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
    vedtakRepository.findById(stonadsendring.vedtak.id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", stonadsendring.vedtak.id)) }
    return stonadsendringRepository.save(stonadsendring)
  }

  fun hentAlleStonadsendringerForVedtak(id: Int): List<Stonadsendring> {
    return stonadsendringRepository.hentAlleStonadsendringerForVedtak(id)
  }

  fun opprettPeriode(periode: Periode): Periode {
    stonadsendringRepository.findById(periode.stonadsendring.id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke stønadsendring med id %d i databasen", periode.stonadsendring.id)) }
    return periodeRepository.save(periode)
  }

  fun hentAllePerioderForStonadsendring(id: Int): List<Periode> {
    return periodeRepository.hentAllePerioderForStonadsendring(id)
  }

  fun opprettGrunnlag(grunnlag: Grunnlag): Grunnlag {
    vedtakRepository.findById(grunnlag.vedtak.id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", grunnlag.vedtak.id)) }
    return grunnlagRepository.save(grunnlag)
  }

  fun hentGrunnlag(id: Int): Grunnlag {
    val grunnlag = grunnlagRepository.findById(id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", id)) }
    return grunnlag
  }

  fun hentAlleGrunnlagForVedtak(id: Int): List<Grunnlag> {
    return grunnlagRepository.hentAlleGrunnlagForVedtak(id)
  }

  fun opprettPeriodeGrunnlag(periodeGrunnlagBo: PeriodeGrunnlagBo): PeriodeGrunnlag {
    val eksisterendePeriode = periodeRepository.findById(periodeGrunnlagBo.periodeId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke periode med id %d i databasen", periodeGrunnlagBo.periodeId)) }
    val eksisterendeGrunnlag = grunnlagRepository.findById(periodeGrunnlagBo.grunnlagId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", periodeGrunnlagBo.grunnlagId)) }
    val nyttPeriodeGrunnlag = PeriodeGrunnlag(eksisterendePeriode, eksisterendeGrunnlag)
    SECURE_LOGGER.info("bidrag-vedtak - nyttPeriodeGrunnlag: $nyttPeriodeGrunnlag")
    return periodeGrunnlagRepository.save(nyttPeriodeGrunnlag)
  }

  fun hentAlleGrunnlagForPeriode(periodeId: Int): List<PeriodeGrunnlag> {
    return periodeGrunnlagRepository.hentAlleGrunnlagForPeriode(periodeId)
  }

  fun opprettEngangsbelop(engangsbelop: Engangsbelop): Engangsbelop {
    vedtakRepository.findById(engangsbelop.vedtak.id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", engangsbelop.vedtak.id)) }
    return engangsbelopRepository.save(engangsbelop)
  }

  fun hentAlleEngangsbelopForVedtak(id: Int): List<Engangsbelop> {
    return engangsbelopRepository.hentAlleEngangsbelopForVedtak(id)
  }

  fun opprettEngangsbelopGrunnlag(engangsbelopGrunnlagBo: EngangsbelopGrunnlagBo): EngangsbelopGrunnlag {
    val eksisterendeEngangsbelop = engangsbelopRepository.findById(engangsbelopGrunnlagBo.engangsbelopId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke engangsbelop med id %d i databasen", engangsbelopGrunnlagBo.engangsbelopId)) }
    val eksisterendeGrunnlag = grunnlagRepository.findById(engangsbelopGrunnlagBo.grunnlagId)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", engangsbelopGrunnlagBo.grunnlagId)) }
    val nyttEngangsbelopGrunnlag = EngangsbelopGrunnlag(eksisterendeEngangsbelop, eksisterendeGrunnlag)
    SECURE_LOGGER.info("nyttEngangsbelopGrunnlag: $nyttEngangsbelopGrunnlag")
    return engangsbelopGrunnlagRepository.save(nyttEngangsbelopGrunnlag)
  }

  fun hentAlleGrunnlagForEngangsbelop(engangsbelopId: Int): List<EngangsbelopGrunnlag> {
    return engangsbelopGrunnlagRepository.hentAlleGrunnlagForEngangsbelop(engangsbelopId)
  }

  fun opprettBehandlingsreferanse(behandlingsreferanse: Behandlingsreferanse): Behandlingsreferanse {
    vedtakRepository.findById(behandlingsreferanse.vedtak.id)
      .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", behandlingsreferanse.vedtak.id)) }
    return behandlingsreferanseRepository.save(behandlingsreferanse)
  }

  fun hentAlleBehandlingsreferanserForVedtak(id: Int): List<Behandlingsreferanse> {
    return behandlingsreferanseRepository.hentAlleBehandlingsreferanserForVedtak(id)
  }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(PersistenceService::class.java)
  }
}
