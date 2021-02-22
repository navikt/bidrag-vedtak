package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Periode
import org.springframework.data.repository.CrudRepository

interface PeriodeRepository : CrudRepository<Periode, Int?>
