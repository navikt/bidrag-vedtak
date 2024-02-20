package no.nav.bidrag.vedtak.service

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.vedtak.Beslutningstype
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.organisasjon.Enhetsnummer
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.util.visningsnavn
import no.nav.bidrag.transport.behandling.felles.grunnlag.BaseGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BostatusPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBarnIHusstand
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningInntekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.InnhentetAinntekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.InnhentetHusstandsmedlem
import no.nav.bidrag.transport.behandling.felles.grunnlag.InnhentetSkattegrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.NotatGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.SivilstandPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningForskudd
import no.nav.bidrag.transport.behandling.felles.grunnlag.erPerson
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerBasertPåFremmedReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.innholdTilObjekt
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettVedtakRequestDto
import no.nav.bidrag.transport.behandling.vedtak.response.BehandlingsreferanseDto
import no.nav.bidrag.transport.behandling.vedtak.response.EngangsbeløpDto
import no.nav.bidrag.transport.behandling.vedtak.response.StønadsendringDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakPeriodeDto
import no.nav.bidrag.transport.felles.commonObjectmapper
import no.nav.bidrag.transport.felles.toCompactString
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

fun MutableMap<String, MutableList<String>>.merge(with: Map<String, MutableList<String>>) {
    with.forEach { (key, value) ->
        val list = getOrDefault(key, mutableListOf())
        list.addAll(value)
        put(key, list)
    }
}

fun MutableMap<String, MutableList<String>>.add(subgraph: String, value: String) {
    val list = getOrDefault(subgraph, mutableListOf())
    list.add(value)
    put(subgraph, list)
}

enum class MermaidSubgraph {
    STONADSENDRING,
    PERIODE,
    NOTAT,
    SJABLON,
    PERSON,
    INGEN,
}

enum class TreeChildType {
    FRITTSTÅENDE,
    VEDTAK,
    STØNADSENDRING,
    PERIODE,
    GRUNNLAG,
}

data class TreeChild(
    val name: String,
    val id: String,
    val type: TreeChildType,
    val children: MutableList<TreeChild> = mutableListOf(),
    @JsonIgnore
    val parent: TreeChild?,
    val grunnlag: BaseGrunnlag? = null,
    val periode: TreePeriode? = null,
    val stønad: TreeStønad? = null,
    val vedtak: TreeVedtak? = null,
) {
    val grunnlagstype get() = grunnlag?.type
}

data class TreeVedtak(
    val kilde: Vedtakskilde,
    val type: Vedtakstype,
    val opprettetAv: String,
    val opprettetAvNavn: String?,
    val kildeapplikasjon: String,
    val vedtakstidspunkt: LocalDateTime,
    val enhetsnummer: Enhetsnummer?,
    val innkrevingUtsattTilDato: LocalDate?,
    val fastsattILand: String?,
    val opprettetTidspunkt: LocalDateTime,
)

data class TreeStønad(
    val type: Stønadstype,
    val sak: Saksnummer,
    val skyldner: Personident,
    val kravhaver: Personident,
    val mottaker: Personident,
    val førsteIndeksreguleringsår: Int?,
    val innkreving: Innkrevingstype,
    val beslutning: Beslutningstype,
    val omgjørVedtakId: Int?,
    val eksternReferanse: String?,
)

data class TreePeriode(
    val beløp: BigDecimal?,
    val valutakode: String?,
    val resultatkode: String,
    val delytelseId: String?,
)

fun OpprettVedtakRequestDto.toMermaid(): List<String> {
    return tilVedtakDto().toMermaid()
}

fun OpprettVedtakRequestDto.toTree(): TreeChild {
    return tilVedtakDto().toTree()
}

fun Map<String, List<String>>.toMermaid(): List<String> {
    val printList = mutableListOf<String>()
    entries.sortedWith { a, b -> if (a.key.contains("Stønadsendring_")) 1 else -1 }.forEach {
        if (it.key != MermaidSubgraph.INGEN.name) {
            printList.add("\tsubgraph ${it.key}\n")
            if (it.key == MermaidSubgraph.SJABLON.name || it.key == MermaidSubgraph.NOTAT.name) {
                printList.add("\tdirection TB\n")
            }
            printList.addAll(it.value.map { "\t\t$it\n" })
            printList.add("\tend\n")
        } else {
            printList.addAll(it.value.map { "\t$it\n" })
        }
    }
    return printList
}

fun VedtakDto.toMermaid(): List<String> {
    val printList = mutableListOf<String>()
    printList.add("\nflowchart LR\n")
    printList.addAll(
        toTree().toMermaidSubgraphMap().toMermaid().removeDuplicates(),
    )
    return printList
}

fun TreeChild.tilSubgraph(): String? = when (type) {
    TreeChildType.STØNADSENDRING -> "Stønadsendring_${name.removeParanteses()}"
    TreeChildType.PERIODE -> parent?.tilSubgraph()
    TreeChildType.GRUNNLAG ->
        when (grunnlagstype) {
            Grunnlagstype.SJABLON -> MermaidSubgraph.SJABLON.name
            Grunnlagstype.NOTAT -> MermaidSubgraph.NOTAT.name
            Grunnlagstype.SLUTTBEREGNING_FORSKUDD -> parent?.tilSubgraph()
            Grunnlagstype.DELBEREGNING_INNTEKT -> "Delberegning"
            Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND -> "Delberegning"
            else -> if (this.name.startsWith("PERSON_")) MermaidSubgraph.PERSON.name else "Delberegning"
        }

    else -> MermaidSubgraph.INGEN.name
}

fun String.removeParanteses() = this.replace("(", " ").replace(")", "")

fun TreeChild.toMermaidSubgraphMap(parent: TreeChild? = null): Map<String, MutableList<String>> {
    val mermaidSubgraphMap = mutableMapOf<String, MutableList<String>>()

    if (type == TreeChildType.FRITTSTÅENDE) return emptyMap()
    if (parent != null) {
        if (parent.type == TreeChildType.PERIODE) {
            mermaidSubgraphMap.add(
                parent.tilSubgraph()!!,
                "${parent.id}[[${parent.name.removeParanteses()}]] --> $id",
            )
        } else if (type == TreeChildType.GRUNNLAG) {
            if (grunnlagstype == Grunnlagstype.SJABLON || grunnlag?.erPerson() == true || grunnlagstype == Grunnlagstype.NOTAT) {
                val subgraph = tilSubgraph()
//                mermaidSubgraphMap.add(
//                    subgraph!!,
//                    "$id[${name.removeParanteses()}] ~~~ END",
//                )
//                mermaidSubgraphMap.add(
//                    parent.tilSubgraph()!!,
//                    "${parent.id}[${parent.name}] -.- $subgraph",
//                )
            } else if (grunnlagstype == Grunnlagstype.SLUTTBEREGNING_FORSKUDD) {
                mermaidSubgraphMap.add(
                    tilSubgraph()!!,
                    "${parent.id}[${parent.name.removeParanteses()}] -->$id{${name.removeParanteses()}}",
                )
            } else if (parent.grunnlagstype == Grunnlagstype.SLUTTBEREGNING_FORSKUDD && (
                    grunnlagstype == Grunnlagstype.DELBEREGNING_INNTEKT ||
                        grunnlagstype == Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND
                    )
            ) {
                mermaidSubgraphMap.add(
                    parent.tilSubgraph()!!,
                    "${parent.id}[${parent.name.removeParanteses()}] -->|${name.removeParanteses()}| $id[[${name.removeParanteses()}]]",
                )
            } else if (parent.grunnlagstype == Grunnlagstype.DELBEREGNING_INNTEKT ||
                parent.grunnlagstype == Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND
            ) {
                mermaidSubgraphMap.add(
                    parent.tilSubgraph()!!,
                    "${parent.id}[[${parent.name.removeParanteses()}]] --> $id[${name.removeParanteses()}]",
                )
            } else {
                mermaidSubgraphMap.add(
                    parent.tilSubgraph()!!,
                    "${parent.id}[${parent.name.removeParanteses()}] --> $id[${name.removeParanteses()}]",
                )
            }
        } else {
            mermaidSubgraphMap.add(
                parent.tilSubgraph()!!,
                "${parent.id}[${parent.name.removeParanteses()}] --> $id[${name.removeParanteses()}]",
            )
        }
    }

    children.forEach { mermaidSubgraphMap.merge(it.toMermaidSubgraphMap(this)) }

    return mermaidSubgraphMap
}

fun List<String>.removeDuplicates(): List<String> {
    val distinctList = mutableListOf<String>()
    val ignoreList = listOf("subgraph", "\tend", "flowchart")
    this.forEach {
        if (ignoreList.any { ignore -> it.contains(ignore) }) {
            distinctList.add(it)
        } else if (!distinctList.contains(it)) {
            distinctList.add(it)
        }
    }
    return distinctList
}

fun VedtakDto.toTree(): TreeChild {
    val vedtakParent =
        TreeChild(
            id = "Vedtak",
            name = "Vedtak",
            type = TreeChildType.VEDTAK,
            parent = null,
            vedtak =
            TreeVedtak(
                kilde = kilde,
                type = type,
                opprettetAv = opprettetAv ?: "",
                opprettetAvNavn = opprettetAv,
                kildeapplikasjon = "behandling",
                vedtakstidspunkt = vedtakstidspunkt,
                enhetsnummer = enhetsnummer,
                innkrevingUtsattTilDato = innkrevingUtsattTilDato,
                fastsattILand = fastsattILand,
                opprettetTidspunkt = LocalDateTime.now(),
            ),
        )
    val grunnlagSomIkkeErReferert =
        TreeChild(
            id = "ikke_referert",
            name = "Frittstående(Ikke refert av grunnlag eller stønadsendring)",
            parent = vedtakParent,
            type = TreeChildType.FRITTSTÅENDE,
        )
    vedtakParent.children.add(grunnlagSomIkkeErReferert)
    grunnlagSomIkkeErReferert.children.addAll(
        grunnlagListe.filter {
            grunnlagListe.filtrerBasertPåFremmedReferanse(referanse = it.referanse).isEmpty()
        }.filter {
            !stønadsendringListe.flatMap { it.grunnlagReferanseListe }.contains(it.referanse)
        }
            .filter {
                !stønadsendringListe.flatMap { it.periodeListe.flatMap { it.grunnlagReferanseListe } }
                    .contains(it.referanse)
            }
            .map {
                it.referanse.toTree(grunnlagListe, vedtakParent)
            }.filterNotNull(),
    )
    stønadsendringListe.forEachIndexed { i, st ->
        val stønadsendringId =
            "Stønadsendring_${st.type}_${st.kravhaver.verdi}"
        val stønadsendringTree =
            TreeChild(
                id = stønadsendringId,
                name = "Stønadsendring Barn ${i + 1}",
                type = TreeChildType.STØNADSENDRING,
                parent = vedtakParent,
                stønad =
                TreeStønad(
                    type = st.type,
                    sak = st.sak,
                    skyldner = st.skyldner,
                    kravhaver = st.kravhaver,
                    mottaker = st.mottaker,
                    førsteIndeksreguleringsår = st.førsteIndeksreguleringsår,
                    innkreving = st.innkreving,
                    beslutning = st.beslutning,
                    omgjørVedtakId = st.omgjørVedtakId,
                    eksternReferanse = st.eksternReferanse,
                ),
            )
        vedtakParent.children.add(stønadsendringTree)
        stønadsendringTree.children.addAll(
            st.grunnlagReferanseListe.toTree(
                grunnlagListe,
                stønadsendringTree,
            ),
        )
        st.periodeListe.forEach {
            val periodeId = "Periode${it.periode.fom.toCompactString()}${st.kravhaver.verdi}"
            val periodeTree =
                TreeChild(
                    id = periodeId,
                    name = "Periode(${it.periode.fom.toCompactString()})",
                    type = TreeChildType.PERIODE,
                    parent = stønadsendringTree,
                    periode =
                    TreePeriode(
                        beløp = it.beløp,
                        valutakode = it.valutakode,
                        resultatkode = it.resultatkode,
                        delytelseId = it.delytelseId,
                    ),
                )

            periodeTree.children.addAll(
                it.grunnlagReferanseListe.toTree(
                    grunnlagListe,
                    periodeTree,
                ).toMutableList(),
            )
            stønadsendringTree.children.add(periodeTree)
        }
    }
    return vedtakParent
}

fun List<Grunnlagsreferanse>.toTree(grunnlagsListe: List<BaseGrunnlag>, parent: TreeChild?): List<TreeChild> {
    return map {
        it.toTree(grunnlagsListe, parent)
    }.filterNotNull()
}

fun Grunnlagsreferanse.toTree(grunnlagsListe: List<BaseGrunnlag>, parent: TreeChild?): TreeChild? {
    val grunnlagListe = grunnlagsListe.filtrerBasertPåEgenReferanse(referanse = this)
    if (grunnlagListe.isEmpty()) {
        return null
    }

    val grunnlag = grunnlagListe.first()
    val treeMap =
        grunnlagListe.flatMap {
            it.grunnlagsreferanseListe.map { it.toTree(grunnlagsListe, parent) } +
                it.gjelderReferanse?.toTree(grunnlagsListe, parent)
        }

    return TreeChild(
        name =
        when (grunnlag.type) {
            Grunnlagstype.SLUTTBEREGNING_FORSKUDD ->
                "Sluttberegning" +
                    "(${grunnlag.innholdTilObjekt<SluttberegningForskudd>().periode.fom.toCompactString()})"
            Grunnlagstype.SJABLON ->
                "Sjablon(" +
                    "${commonObjectmapper.readTree(commonObjectmapper.writeValueAsString(grunnlag.innhold)).get("sjablonNavn")})"

            Grunnlagstype.DELBEREGNING_INNTEKT ->
                "Delberegning inntekt " +
                    grunnlag.innholdTilObjekt<DelberegningInntekt>().periode.fom.toCompactString()

            Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND ->
                "Delberegning barn i husstand(" +
                    grunnlag.innholdTilObjekt<DelberegningBarnIHusstand>().periode.fom.toCompactString() + ")"

            Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE ->
                "Inntektsrapportering(" +
                    "${grunnlag.innholdTilObjekt<InntektsrapporteringPeriode>().inntektsrapportering})"

            Grunnlagstype.SIVILSTAND_PERIODE ->
                "Sivilstand(" +
                    "${grunnlag.innholdTilObjekt<SivilstandPeriode>().sivilstand.visningsnavn.intern})"

            Grunnlagstype.BOSTATUS_PERIODE ->
                "Bosstatus(" +
                    "${grunnlag.innholdTilObjekt<BostatusPeriode>().bostatus.visningsnavn.intern})"

            Grunnlagstype.NOTAT -> "Notat(${grunnlag.innholdTilObjekt<NotatGrunnlag>().type})"
            Grunnlagstype.INNHENTET_HUSSTANDSMEDLEM ->
                "Innhentet husstandsmedlem(" +
                    grunnlag.innholdTilObjekt<InnhentetHusstandsmedlem>().grunnlag.fødselsdato?.toCompactString() + ")"

            Grunnlagstype.INNHENTET_SIVILSTAND -> "Innhentet sivilstand"
            Grunnlagstype.INNHENTET_ARBEIDSFORHOLD -> "Innhentet arbeidsforhold"
            Grunnlagstype.INNHENTET_INNTEKT_SKATTEGRUNNLAG_PERIODE ->
                "Innhentet skattegrunnlag(" +
                    grunnlag.innholdTilObjekt<InnhentetSkattegrunnlag>().periode.fom.toCompactString() + ")"

            Grunnlagstype.INNHENTET_INNTEKT_AINNTEKT_PERIODE ->
                "Innhentet ainntekt(" +
                    grunnlag.innholdTilObjekt<InnhentetAinntekt>().periode.fom.toCompactString() + ")"

            else ->
                if (grunnlag.erPerson()) {
                    "${grunnlag.type}(${grunnlag.innholdTilObjekt<Person>().fødselsdato.toCompactString()})"
                } else {
                    this
                }
        },
        id = this,
        grunnlag = grunnlag,
        type = TreeChildType.GRUNNLAG,
        parent = parent,
        children = treeMap.filterNotNull().toMutableList(),
    )
}

fun OpprettVedtakRequestDto.tilVedtakDto(): VedtakDto {
    return VedtakDto(
        type = type,
        opprettetAv = opprettetAv ?: "",
        opprettetAvNavn = opprettetAv,
        kilde = kilde,
        kildeapplikasjon = "behandling",
        vedtakstidspunkt = vedtakstidspunkt,
        enhetsnummer = enhetsnummer,
        innkrevingUtsattTilDato = innkrevingUtsattTilDato,
        fastsattILand = fastsattILand,
        opprettetTidspunkt = LocalDateTime.now(),
        behandlingsreferanseListe =
        behandlingsreferanseListe.map {
            BehandlingsreferanseDto(
                kilde = it.kilde,
                referanse = it.referanse,
            )
        },
        stønadsendringListe =
        stønadsendringListe.map {
            StønadsendringDto(
                innkreving = it.innkreving,
                skyldner = it.skyldner,
                kravhaver = it.kravhaver,
                mottaker = it.mottaker,
                sak = it.sak,
                type = it.type,
                beslutning = it.beslutning,
                grunnlagReferanseListe = it.grunnlagReferanseListe,
                eksternReferanse = it.eksternReferanse,
                omgjørVedtakId = it.omgjørVedtakId,
                førsteIndeksreguleringsår = it.førsteIndeksreguleringsår,
                periodeListe =
                it.periodeListe.map {
                    VedtakPeriodeDto(
                        periode = it.periode,
                        beløp = it.beløp,
                        valutakode = it.valutakode,
                        resultatkode = it.resultatkode,
                        delytelseId = it.delytelseId,
                        grunnlagReferanseListe = it.grunnlagReferanseListe,
                    )
                },
            )
        },
        engangsbeløpListe =
        engangsbeløpListe.map {
            EngangsbeløpDto(
                beløp = it.beløp,
                valutakode = it.valutakode,
                resultatkode = it.resultatkode,
                delytelseId = it.delytelseId,
                grunnlagReferanseListe = it.grunnlagReferanseListe,
                beslutning = it.beslutning,
                innkreving = it.innkreving,
                skyldner = it.skyldner,
                kravhaver = it.kravhaver,
                mottaker = it.mottaker,
                sak = it.sak,
                type = it.type,
                eksternReferanse = it.eksternReferanse,
                omgjørVedtakId = it.omgjørVedtakId,
                referanse = it.referanse ?: "",
            )
        },
        grunnlagListe =
        grunnlagListe.map {
            GrunnlagDto(
                referanse = it.referanse,
                type = it.type,
                innhold = it.innhold,
                grunnlagsreferanseListe = it.grunnlagsreferanseListe,
                gjelderReferanse = it.gjelderReferanse,
            )
        },
    )
}
