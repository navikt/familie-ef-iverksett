package no.nav.familie.ef.iverksett.iverksetting.domene

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import no.nav.familie.kontrakter.ef.felles.BehandlingType
import no.nav.familie.kontrakter.ef.felles.BehandlingÅrsak
import no.nav.familie.kontrakter.ef.felles.OpphørÅrsak
import no.nav.familie.kontrakter.ef.felles.RegelId
import no.nav.familie.kontrakter.ef.felles.Vedtaksresultat
import no.nav.familie.kontrakter.ef.felles.VilkårType
import no.nav.familie.kontrakter.ef.felles.Vilkårsresultat
import no.nav.familie.kontrakter.ef.iverksett.AdressebeskyttelseGradering
import no.nav.familie.kontrakter.ef.iverksett.AktivitetType
import no.nav.familie.kontrakter.ef.iverksett.IverksettBarnetilsynDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettDto
import no.nav.familie.kontrakter.ef.iverksett.IverksettOvergangsstønadDto
import no.nav.familie.kontrakter.ef.iverksett.SvarId
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeBarnetilsynDto
import no.nav.familie.kontrakter.ef.iverksett.VedtaksperiodeType
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.tilbakekreving.Periode
import no.nav.familie.kontrakter.felles.tilbakekreving.Tilbakekrevingsvalg
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import no.nav.familie.kontrakter.ef.iverksett.Brevmottaker as BrevmottakerKontrakter

sealed class Iverksett {

    abstract val fagsak: Fagsakdetaljer
    abstract val behandling: Behandlingsdetaljer
    abstract val søker: Søker
    abstract val vedtak: Vedtaksdetaljer

    fun erMigrering(): Boolean = behandling.behandlingÅrsak == BehandlingÅrsak.MIGRERING

    abstract fun medNyTilbakekreving(nyTilbakekreving: Tilbakekrevingsdetaljer?): Iverksett

}

data class IverksettOvergangsstønad(
        override val fagsak: Fagsakdetaljer,
        override val behandling: Behandlingsdetaljer,
        override val søker: Søker,
        override val vedtak: VedtaksdetaljerOvergangsstønad,
) : Iverksett() {

    override fun medNyTilbakekreving(nyTilbakekreving: Tilbakekrevingsdetaljer?): IverksettOvergangsstønad {
        return this.copy(vedtak = this.vedtak.copy(tilbakekreving = nyTilbakekreving))
    }

}

data class IverksettBarnetilsyn(
        override val fagsak: Fagsakdetaljer,
        override val behandling: Behandlingsdetaljer,
        override val søker: Søker,
        override val vedtak: VedtaksdetaljerBarnetilsyn,
) : Iverksett() {

    override fun medNyTilbakekreving(nyTilbakekreving: Tilbakekrevingsdetaljer?): IverksettBarnetilsyn {
        return this.copy(vedtak = this.vedtak.copy(tilbakekreving = nyTilbakekreving))
    }

}

data class Fagsakdetaljer(
        val fagsakId: UUID,
        val eksternId: Long,
        val stønadstype: StønadType
)

data class Søker(
        val personIdent: String,
        val barn: List<Barn> = ArrayList(),
        val tilhørendeEnhet: String,
        val adressebeskyttelse: AdressebeskyttelseGradering? = null
)

sealed class Vedtaksperiode {

    abstract val fraOgMed: LocalDate
    abstract val tilOgMed: LocalDate
}

data class VedtaksperiodeOvergangsstønad(
        override val fraOgMed: LocalDate,
        override val tilOgMed: LocalDate,
        val aktivitet: AktivitetType,
        val periodeType: VedtaksperiodeType) : Vedtaksperiode() {
}

data class VedtaksperiodeBarnetilsyn(
        override val fraOgMed: LocalDate,
        override val tilOgMed: LocalDate,
        val utgifter: BigDecimal,
        val antallBarn: Int) : Vedtaksperiode() {
}

data class PeriodeMedBeløp(
        val fraOgMed: LocalDate,
        val tilOgMed: LocalDate,
        val beløp: Int
)

sealed class Vedtaksdetaljer {

    abstract val vedtaksresultat: Vedtaksresultat
    abstract val vedtakstidspunkt: LocalDateTime
    abstract val opphørÅrsak: OpphørÅrsak?
    abstract val saksbehandlerId: String
    abstract val beslutterId: String
    abstract val tilkjentYtelse: TilkjentYtelse?
    abstract val tilbakekreving: Tilbakekrevingsdetaljer?
    abstract val brevmottakere: Brevmottakere?
    abstract val vedtaksperioder: List<Vedtaksperiode>
}

data class VedtaksdetaljerOvergangsstønad(
        override val vedtaksresultat: Vedtaksresultat,
        override val vedtakstidspunkt: LocalDateTime,
        override val opphørÅrsak: OpphørÅrsak?,
        override val saksbehandlerId: String,
        override val beslutterId: String,
        override val tilkjentYtelse: TilkjentYtelse?,
        override val tilbakekreving: Tilbakekrevingsdetaljer? = null,
        override val brevmottakere: Brevmottakere? = null,
        override val vedtaksperioder: List<VedtaksperiodeOvergangsstønad>
) : Vedtaksdetaljer()

data class VedtaksdetaljerBarnetilsyn(
        override val vedtaksresultat: Vedtaksresultat,
        override val vedtakstidspunkt: LocalDateTime,
        override val opphørÅrsak: OpphørÅrsak?,
        override val saksbehandlerId: String,
        override val beslutterId: String,
        override val tilkjentYtelse: TilkjentYtelse?,
        override val tilbakekreving: Tilbakekrevingsdetaljer? = null,
        override val brevmottakere: Brevmottakere? = null,
        override val vedtaksperioder: List<VedtaksperiodeBarnetilsyn>,
        val kontantstøtte: List<PeriodeMedBeløp>,
        val tilleggsstønad: List<PeriodeMedBeløp>
) : Vedtaksdetaljer()

data class Behandlingsdetaljer(
        val forrigeBehandlingId: UUID? = null,
        val behandlingId: UUID,
        val eksternId: Long,
        val behandlingType: BehandlingType,
        val behandlingÅrsak: BehandlingÅrsak,
        val relatertBehandlingId: UUID? = null,
        val vilkårsvurderinger: List<Vilkårsvurdering> = emptyList(),
        val aktivitetspliktInntrefferDato: LocalDate? = null
)

data class Vilkårsvurdering(
        val vilkårType: VilkårType,
        val resultat: Vilkårsresultat,
        val delvilkårsvurderinger: List<Delvilkårsvurdering> = emptyList()
)

data class Delvilkårsvurdering(
        val resultat: Vilkårsresultat,
        val vurderinger: List<Vurdering> = emptyList()
)

data class Vurdering(
        val regelId: RegelId,
        val svar: SvarId? = null,
        val begrunnelse: String? = null
)


enum class IverksettType {
    VANLIG,
    TEKNISK_OPPHØR
}

data class Tilbakekrevingsdetaljer(
        val tilbakekrevingsvalg: Tilbakekrevingsvalg,
        val tilbakekrevingMedVarsel: TilbakekrevingMedVarsel?
)

data class TilbakekrevingMedVarsel(
        val varseltekst: String,
        val sumFeilutbetaling: BigDecimal?,
        val perioder: List<Periode>?
)

data class Brevmottakere(val mottakere: List<Brevmottaker>)
data class Brevmottaker(
        val ident: String,
        val navn: String,
        val identType: BrevmottakerKontrakter.IdentType,
        val mottakerRolle: BrevmottakerKontrakter.MottakerRolle
)


private class IverksettDeserializer : StdDeserializer<Iverksett>(Iverksett::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): Iverksett {
        val mapper = p.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(p)

        val stønadstype = node.get("fagsak").get("stønadstype").asText()
        return when (StønadType.valueOf(stønadstype)) {
            StønadType.OVERGANGSSTØNAD -> mapper.treeToValue(node, IverksettOvergangsstønad::class.java)
            StønadType.BARNETILSYN -> mapper.treeToValue(node, IverksettBarnetilsyn::class.java)
            else -> error("Har ikke mapping for $stønadstype")
        }
    }
}

class IverksettDtoDeserializer : StdDeserializer<IverksettDto>(IverksettDto::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): IverksettDto {
        val mapper = p.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(p)

        val stønadstype = node.get("fagsak").get("stønadstype").asText()
        return when (StønadType.valueOf(stønadstype)) {
            StønadType.OVERGANGSSTØNAD -> mapper.treeToValue(node, IverksettOvergangsstønadDto::class.java)
            StønadType.BARNETILSYN -> mapper.treeToValue(node, IverksettBarnetilsynDto::class.java)
            else -> error("Har ikke mapping for $stønadstype")
        }
    }
}


class IverksettModule : com.fasterxml.jackson.databind.module.SimpleModule() {
    init {
        addDeserializer(IverksettDto::class.java, IverksettDtoDeserializer())
        addDeserializer(Iverksett::class.java, IverksettDeserializer())
    }
}
