package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import org.springframework.data.jpa.repository.JpaRepository

interface VedtakRepository : JpaRepository<Vedtak, Int?>
