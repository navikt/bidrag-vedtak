package no.nav.bidrag.vedtak.persistence.entity

import java.io.Serializable
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

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

class EngangsbelopGrunnlagPK(val engangsbelop: Int = 0, val grunnlag: Int = 0) : Serializable
