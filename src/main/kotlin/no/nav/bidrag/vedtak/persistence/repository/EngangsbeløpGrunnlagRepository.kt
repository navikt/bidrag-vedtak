package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.EngangsbeløpGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.EngangsbeløpGrunnlagPK
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface EngangsbeløpGrunnlagRepository : CrudRepository<EngangsbeløpGrunnlag, EngangsbeløpGrunnlagPK?> {

    @Query(
        "select ebg from EngangsbeløpGrunnlag ebg " +
            "where ebg.engangsbeløp.id = :engangsbeløpsid and ebg.grunnlag.id = :grunnlagsid",
    )
    fun hentEngangsbeløpGrunnlag(engangsbeløpsid: Int, grunnlagsid: Int): EngangsbeløpGrunnlag

    @Query(
        "select ebg from EngangsbeløpGrunnlag ebg where ebg.engangsbeløp.id = :engangsbeløpsid order by ebg.grunnlag.id",
    )
    fun hentAlleGrunnlagForEngangsbeløp(engangsbeløpsid: Int): List<EngangsbeløpGrunnlag>

    @Modifying
    @Transactional
    @Query(
        "delete from EngangsbeløpGrunnlag er where er.engangsbeløp.id = :engangsbeløpsid",
    )
    fun slettForEngangsbeløp(@Param("engangsbeløpsid") engangsbeløpsid: Int): Int

    @Modifying
    @Transactional
    @Query(
        value = "delete from EngangsbeløpGrunnlag eg" +
            " where eg.engangsbeløpsid in (" +
            " select e.engangsbeløpsid from engangsbeløp e " +
            " join Vedtak v on e.vedtaksid = v.vedtaksid " +
            " where v.vedtaksid = :vedtaksid" +
            ")",
        nativeQuery = true,
    )
    fun slettAlleEngangsbeløpGrunnlagForVedtak(vedtaksid: Int)
}
