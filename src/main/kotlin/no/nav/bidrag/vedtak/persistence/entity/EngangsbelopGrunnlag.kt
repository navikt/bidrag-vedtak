package no.nav.bidrag.vedtak.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.io.Serializable

@IdClass(EngangsbelopGrunnlagPK::class)
@Entity
@Table(name = "engangsbelopgrunnlag")
data class EngangsbelopGrunnlag(

    @Id
    @ManyToOne
    @JoinColumn(name = "engangsbelop_id")
    val engangsbelop: Engangsbelop = Engangsbelop(),

    @Id
    @ManyToOne
    @JoinColumn(name = "grunnlag_id")
    val grunnlag: Grunnlag = Grunnlag()

)

data class EngangsbelopGrunnlagPK(val engangsbelop: Int = 0, val grunnlag: Int = 0) : Serializable
