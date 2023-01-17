package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface GrunnlagRepository : JpaRepository<Grunnlag, Int?>{

  @Query(
    "select gr from Grunnlag gr where gr.vedtak.id = :vedtakId order by gr.id"
  )
  fun hentAlleGrunnlagForVedtak(vedtakId: Int): List<Grunnlag>
}
