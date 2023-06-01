package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface GrunnlagRepository : JpaRepository<Grunnlag, Int?> {

    @Query(
        "select gr from Grunnlag gr where gr.vedtak.id = :vedtakId order by gr.id"
    )
    fun hentAlleGrunnlagForVedtak(vedtakId: Int): List<Grunnlag>

    @Modifying
    @Query(
        "delete from Grunnlag gr where gr.vedtak.id = :vedtakId"
    )
    fun slettAlleGrunnlagForVedtak(@Param("vedtakId") vedtakId: Int):Int
}
